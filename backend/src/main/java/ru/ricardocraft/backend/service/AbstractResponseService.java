package ru.ricardocraft.backend.service;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.UUID;

public abstract class AbstractResponseService {

    protected final WebSocketService service;

    protected final Class<? extends SimpleResponse> responseClass;

    protected AbstractResponseService(Class<? extends SimpleResponse> responseClass, WebSocketService service) {
        this.service = service;
        this.responseClass = responseClass;
        service.registerService(responseClass, this);
    }

    abstract public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client)  throws Exception;

    @SuppressWarnings("unchecked")
    public <Response extends SimpleResponse> Response castResponse(SimpleResponse response) throws Exception {
        if (responseClass.isAssignableFrom(response.getClass())) return (Response) response;
        else throw new Exception("Cannot cast " + response.getClass() + " to " + responseClass.getName());
    }

    public void sendResult(ChannelHandlerContext ctx, RequestEvent result, UUID requestUUID) {
        result.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), result);
    }

    public void sendResultAndClose(ChannelHandlerContext ctx, RequestEvent result, UUID requestUUID) {
        result.requestUUID = requestUUID;
        service.sendObjectAndClose(ctx, result);
    }

    public void sendError(ChannelHandlerContext ctx, String errorMessage, UUID requestUUID) {
        ErrorRequestEvent event = new ErrorRequestEvent(errorMessage);
        event.requestUUID = requestUUID;
        service.sendObject(ctx.channel(), event);
    }
}
