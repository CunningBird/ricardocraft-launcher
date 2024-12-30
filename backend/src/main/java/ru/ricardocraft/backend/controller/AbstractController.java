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

    abstract public AbstractResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception;

    @SuppressWarnings("unchecked")
    public <Request extends AbstractRequest> Request castRequest(AbstractRequest request) throws Exception {
        if (requestClass.isAssignableFrom(request.getClass())) return (Request) request;
        else throw new Exception("Cannot cast " + request.getClass() + " to " + requestClass.getName());
    }
}
