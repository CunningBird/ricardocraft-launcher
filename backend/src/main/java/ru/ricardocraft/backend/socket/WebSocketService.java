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
import ru.ricardocraft.backend.utils.BiHookSet;
import ru.ricardocraft.backend.utils.HookSet;

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
    private final HookSet<WebSocketRequestContext> hookBeforeParsing = new HookSet<>();
    private final HookSet<WebSocketRequestContext> hookBeforeExecute = new HookSet<>();
    private final HookSet<WebSocketRequestContext> hookComplete = new HookSet<>();
    private final BiHookSet<Channel, Object> hookSend = new BiHookSet<>();
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
        if (hookBeforeParsing.hook(context)) {
            return;
        }
        WebSocketServerResponse response = gson.fromJson(request, WebSocketServerResponse.class);
        context.response = response;
        if (response == null) {
            RequestEvent event = new ErrorRequestEvent("This type of request is not supported");
            hookComplete.hook(context);
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
        if (hookBeforeExecute.hook(context)) {
            return;
        }
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
        hookComplete.hook(context);
    }

    public void registerClient(Channel channel) {
        channels.add(channel);
    }

    public void sendObject(Channel channel, Object obj) {
        if (hookSend.hook(channel, obj)) {
            return;
        }
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
        }
        channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
    }

    public void sendObject(Channel channel, Object obj, Type type) {
        if (hookSend.hook(channel, obj)) {
            return;
        }
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

    public void sendObjectToUUID(UUID userUuid, Object obj, Type type) {
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            Client client = wsHandler.getClient();
            if (client == null || !userUuid.equals(client.uuid)) continue;
            if (hookSend.hook(ch, obj)) {
                continue;
            }
            String msg = gson.toJson(obj, type);
            if (logger.isTraceEnabled()) {
                logger.trace("Send to {}({}): {}", getIPFromChannel(ch), userUuid, msg);
            }
            ch.writeAndFlush(new TextWebSocketFrame(msg), ch.voidPromise());
        }
    }

    public Channel getChannelFromConnectUUID(UUID connectUuid) {
        for (Channel ch : channels) {
            if (ch == null || ch.pipeline() == null) continue;
            WebSocketFrameHandler wsHandler = ch.pipeline().get(WebSocketFrameHandler.class);
            if (wsHandler == null) continue;
            if (connectUuid.equals(wsHandler.getConnectUUID())) {
                return ch;
            }
        }
        return null;
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj) {
        if (hookSend.hook(ctx.channel(), obj)) {
            return;
        }
        String msg = gson.toJson(obj, WebSocketEvent.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Send and close {}: {}", getIPFromContext(ctx), msg);
        }
        ctx.writeAndFlush(new TextWebSocketFrame(msg)).addListener(ChannelFutureListener.CLOSE);
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj, Type type) {
        if (hookSend.hook(ctx.channel(), obj)) {
            return;
        }
        String msg = gson.toJson(obj, type);
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

    public static class EventResult implements WebSocketEvent {
        public EventResult() {

        }

        @Override
        public String getType() {
            return "event";
        }
    }

}
