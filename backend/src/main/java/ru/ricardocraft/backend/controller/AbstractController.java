package ru.ricardocraft.backend.controller;

import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;

public abstract class AbstractController {

    protected final ServerWebSocketHandler handler;

    protected final Class<? extends AbstractRequest> requestClass;

    protected AbstractController(Class<? extends AbstractRequest> requestClass, ServerWebSocketHandler handler) {
        this.handler = handler;
        this.requestClass = requestClass;
        handler.registerService(requestClass, this);
    }

    abstract public AbstractResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception;

    @SuppressWarnings("unchecked")
    public <Response extends AbstractRequest> Response castResponse(AbstractRequest response) throws Exception {
        if (requestClass.isAssignableFrom(response.getClass())) return (Response) response;
        else throw new Exception("Cannot cast " + response.getClass() + " to " + requestClass.getName());
    }
}
