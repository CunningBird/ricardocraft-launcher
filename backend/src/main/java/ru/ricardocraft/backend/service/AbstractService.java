package ru.ricardocraft.backend.service;

import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

public abstract class AbstractService {

    protected final ServerWebSocketHandler handler;

    protected final Class<? extends AbstractRequest> responseClass;

    protected AbstractService(Class<? extends AbstractRequest> responseClass, ServerWebSocketHandler handler) {
        this.handler = handler;
        this.responseClass = responseClass;
        handler.registerService(responseClass, this);
    }

    abstract public AbstractResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception;

    @SuppressWarnings("unchecked")
    public <Response extends AbstractRequest> Response castResponse(AbstractRequest response) throws Exception {
        if (responseClass.isAssignableFrom(response.getClass())) return (Response) response;
        else throw new Exception("Cannot cast " + response.getClass() + " to " + responseClass.getName());
    }
}
