package ru.ricardocraft.backend.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Component
public class WebSocketService {

    private transient final Logger logger = LogManager.getLogger(WebSocketService.class);

    public final ChannelGroup channels;
    private final ExecutorService executors;

    private final Map<Class<? extends SimpleResponse>, AbstractResponseService> services = new HashMap<>();

    private transient final NettyProperties nettyProperties;
    private transient final JacksonManager jacksonManager;

    @Autowired
    public WebSocketService(NettyProperties nettyProperties, JacksonManager jacksonManager) {
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        executors = switch (nettyProperties.getPerformance().getExecutorType()) {
            case NONE -> null;
            case DEFAULT -> Executors.newCachedThreadPool();
            case WORK_STEAL -> Executors.newWorkStealingPool();
            case VIRTUAL_THREADS -> Executors.newVirtualThreadPerTaskExecutor();
        };

        this.nettyProperties = nettyProperties;
        this.jacksonManager = jacksonManager;
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

    public void process(ChannelHandlerContext ctx, TextWebSocketFrame frame, Client client, String ip, UUID connectUUID) throws JsonProcessingException {
        String request = frame.text();
        WebSocketRequestContext context = new WebSocketRequestContext(ctx, request, client, ip, connectUUID);
        SimpleResponse response = jacksonManager.getMapper().readValue(request, SimpleResponse.class);
        context.response = response;
        if (response == null) {
            RequestEvent event = new ErrorRequestEvent("This type of request is not supported");
            sendObject(ctx.channel(), event);
            return;
        }
        var safeStatus = nettyProperties.getPerformance().getDisableThreadSafeClientObject() ? SimpleResponse.ThreadSafeStatus.NONE : response.getThreadSafeStatus();
        if (executors == null) {
            process(safeStatus, client, ip, context, response);
        } else {
            executors.submit(() -> process(safeStatus, client, ip, context, response));
        }
    }

    private void process(SimpleResponse.ThreadSafeStatus safeStatus, Client client, String ip, WebSocketRequestContext context, SimpleResponse response) {
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

    void process(WebSocketRequestContext context, SimpleResponse response, Client client, String ip) {
        ChannelHandlerContext ctx = context.context;
        if (ip != null) response.ip = ip;
        else response.ip = IOHelper.getIP(ctx.channel().remoteAddress());
        response.connectUUID = context.connectUUID;
        try {
            RequestEvent requestEvent = services.get(response.getClass()).execute(response, ctx, client);
            sendResult(ctx, requestEvent, response.requestUUID);
        } catch (Throwable e) {
            context.exception = e;
            logger.error("WebSocket request processing failed", e);
            RequestEvent event;
            event = new ErrorRequestEvent(e.getMessage());
            event.requestUUID = response.requestUUID;
            sendObject(ctx.channel(), event);
        }
    }

    public void sendResult(ChannelHandlerContext ctx, RequestEvent result, UUID requestUUID) {
        result.requestUUID = requestUUID;
        sendObject(ctx.channel(), result);
    }

    public void sendResultAndClose(ChannelHandlerContext ctx, RequestEvent result, UUID requestUUID) {
        result.requestUUID = requestUUID;
        sendObjectAndClose(ctx, result);
    }

    public void registerClient(Channel channel) {
        channels.add(channel);
    }

    public void sendObject(Channel channel, Object obj) {
        try {
            String msg = jacksonManager.getMapper().writeValueAsString(obj);
            if (logger.isTraceEnabled()) {
                logger.trace("Send to channel {}: {}", getIPFromChannel(channel), msg);
            }
            channel.writeAndFlush(new TextWebSocketFrame(msg), channel.voidPromise());
        } catch (Exception e) {
            logger.error("Error sending object to channel {}: {}", getIPFromChannel(channel), e.getMessage());
        }
    }

    public void sendObjectAll(Object obj) {
        for (Channel ch : channels) {
            sendObject(ch, obj);
        }
    }

    public void sendObjectAndClose(ChannelHandlerContext ctx, Object obj) {
        try {
            String msg = jacksonManager.getMapper().writeValueAsString(obj);
            if (logger.isTraceEnabled()) {
                logger.trace("Send and close {}: {}", getIPFromContext(ctx), msg);
            }
            ctx.writeAndFlush(new TextWebSocketFrame(msg)).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            logger.error("Error sending object to channel: {}", e.getMessage());
        }
    }

    public static class WebSocketRequestContext {
        public final ChannelHandlerContext context;
        public final String text;
        public final Client client;
        public final String ip;
        public final UUID connectUUID;
        public SimpleResponse response;
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
