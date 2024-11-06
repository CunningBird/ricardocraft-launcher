package ru.ricardocraft.bff.socket.response;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.RequestEvent;
import ru.ricardocraft.bff.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.socket.WebSocketService;

import java.util.UUID;

public abstract class SimpleResponse implements WebSocketServerResponse {
    public UUID requestUUID;
    public transient LaunchServer server;
    public transient WebSocketService service;
    public transient ChannelHandlerContext ctx;
    public transient UUID connectUUID;
    public transient String ip;

    public void sendResult(RequestEvent result) {
        result.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), result);
    }

    public void sendResultAndClose(RequestEvent result) {
        result.requestUUID = requestUUID;
        service.sendObjectAndClose(ctx, result);
    }

    public void sendError(String errorMessage) {
        ErrorRequestEvent event = new ErrorRequestEvent(errorMessage);
        event.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), event);
    }
}
