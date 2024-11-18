package ru.ricardocraft.backend.socket;

import com.google.gson.Gson;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;
import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Component
public class WebSocketService {

    private transient final Logger logger = LogManager.getLogger();

    public final ChannelGroup channels;
    private final ExecutorService executors;

    private final Map<Class<? extends SimpleResponse>, AbstractResponseService> services = new HashMap<>();

    private transient final LaunchServerConfig config;

    private transient final Gson gson;

    @Autowired
    public WebSocketService(LaunchServerConfig config, GsonManager gsonManager) {
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        executors = switch (config.netty.performance.executorType) {
            case NONE -> null;
            case DEFAULT -> Executors.newCachedThreadPool();
            case WORK_STEAL -> Executors.newWorkStealingPool();
            case VIRTUAL_THREADS -> Executors.newVirtualThreadPerTaskExecutor();
        };

        this.config = config;
        this.gson = gsonManager.gson;
    }

    public void registerService(Class<? extends SimpleResponse> responseClass, AbstractResponseService service) {
        services.put(responseClass, service);
    }

    public static String getIPFromContext(ChannelHandlerContext ctx) {
        var handler = ctx.pipeline().get(WebSocketFrameHandler.class);
        if (handler == null || handler.context == null || handler.context.ip == null) {
            return IOHelper.getIP(ctx.channel().remoteAddress());
        }
        return handler.context.ip;
    }

    public static String getIPFromChannel(Channel channel) {
        var handler = channel.pipeline().get(WebSocketFrameHandler.class);
        if (handler == null || handler.context == null || handler.context.ip == null) {
            return IOHelper.getIP(channel.remoteAddress());
        }
        return handler.context.ip;
    }

    public void forEachActiveChannels(BiConsumer<Channel, WebSocketFrameHandler> callback) {
        for (Channel channel : channels) {
            if (channel == null || channel.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = channel.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            callback.accept(channel, wsHandler);
        }
    }

    public void process(ChannelHandlerContext ctx, TextWebSocketFrame frame, Client client, String ip, UUID connectUUID) {
        String request = frame.text();
        WebSocketRequestContext context = new WebSocketRequestContext(ctx, request, client, ip, connectUUID);
        WebSocketServerResponse response = gson.fromJson(request, WebSocketServerResponse.class);
        context.response = response;
        if (response == null) {
            RequestEvent event = new ErrorRequestEvent("This type of request is not supported");
            sendObject(ctx.channel(), event, WebSocketEvent.class);
            return;
        }
        var safeStatus = config.netty.performance.disableThreadSafeClientObject ?
                WebSocketServerResponse.ThreadSafeStatus.NONE : response.getThreadSafeStatus();
        if (executors == null) {
            process(safeStatus, client, ip, context, response);
        } else {
            executors.submit(() -> process(safeStatus, client, ip, context, response));
        }
    }

    private void process(WebSocketServerResponse.ThreadSafeStatus safeStatus, Client client, String ip, WebSocketRequestContext context, WebSocketServerResponse response) {
        switch (safeStatus) {
            case NONE -> process(context, response, client, ip);
            case READ -> {
                var lock = client.lock.readLock();
                lock.lock();
                try {
                    process(context, response, client, ip);
                } finally {
                    lock.unlock();
                }
            }
            case READ_WRITE -> {
                var lock = client.lock.writeLock();
                lock.lock();
                try {
                    process(context, response, client, ip);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    void process(WebSocketRequestContext context, WebSocketServerResponse response, Client client, String ip) {
        ChannelHandlerContext ctx = context.context;
        if (response instanceof SimpleResponse simpleResponse) {
            if (ip != null) simpleResponse.ip = ip;
            else simpleResponse.ip = IOHelper.getIP(ctx.channel().remoteAddress());
            simpleResponse.connectUUID = context.connectUUID;
        }
        try {
            services.get(response.getClass()).execute(response, ctx, client);
        } catch (Throwable e) {
            context.exception = e;
            logger.error("WebSocket request processing failed", e);
            RequestEvent event;
            event = new ErrorRequestEvent("Fatal server error. Contact administrator");
            if (response instanceof SimpleResponse simpleResponse) event.requestUUID = simpleResponse.requestUUID;
            sendObject(ctx.channel(), event);
        }
    }

    public void registerClient(Channel channel) {
        channels.add(channel);
    }

    public void sendObject(Channel channel, Object obj) {
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
        }
        channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
    }

    public void sendObject(Channel channel, Object obj, Type type) {
        String msg = gson.toJson(obj, type);
        if (logger.isTraceEnabled()) {
            logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
        }
        channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
    }

    public void sendObjectAll(Object obj, Type type) {
        for (Channel ch : channels) {
            sendObject(ch, obj, type);
        }
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj) {
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send and close {}: {}", getIPFromContext(ctx), msg);
        }
        ctx.writeAndFlush(new TextWebSocketFrame(msg)).addListener(ChannelFutureListener.CLOSE);
    }

    public static class WebSocketRequestContext {
        public final ChannelHandlerContext context;
        public final String text;
        public final Client client;
        public final String ip;
        public final UUID connectUUID;
        public WebSocketServerResponse response;
        public Throwable exception;

        public WebSocketRequestContext(ChannelHandlerContext context, String text, Client client, String ip, UUID connectUUID) {
            this.context = context;
            this.text = text;
            this.client = client;
            this.ip = ip;
            this.connectUUID = connectUUID;
        }
    }
}
