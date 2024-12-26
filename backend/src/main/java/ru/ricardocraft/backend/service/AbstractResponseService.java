package ru.ricardocraft.backend.service;

import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

public abstract class AbstractResponseService {

    protected final ServerWebSocketHandler handler;

    protected final Class<? extends SimpleResponse> responseClass;

    protected AbstractResponseService(Class<? extends SimpleResponse> responseClass, ServerWebSocketHandler handler) {
        this.handler = handler;
        this.responseClass = responseClass;
        handler.registerService(responseClass, this);
    }

    abstract public RequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception;

    @SuppressWarnings("unchecked")
    public <Response extends SimpleResponse> Response castResponse(SimpleResponse response) throws Exception {
        if (responseClass.isAssignableFrom(response.getClass())) return (Response) response;
        else throw new Exception("Cannot cast " + response.getClass() + " to " + responseClass.getName());
    }
}
