package ru.ricardocraft.backend.service;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
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

    abstract public RequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception;

    @SuppressWarnings("unchecked")
    public <Response extends SimpleResponse> Response castResponse(SimpleResponse response) throws Exception {
        if (responseClass.isAssignableFrom(response.getClass())) return (Response) response;
        else throw new Exception("Cannot cast " + response.getClass() + " to " + responseClass.getName());
    }
}
