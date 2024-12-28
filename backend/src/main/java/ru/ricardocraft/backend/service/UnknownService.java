package ru.ricardocraft.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.UnknownRequest;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class UnknownService extends AbstractService {

    @Autowired
    public UnknownService(ServerWebSocketHandler handler) {
        super(UnknownRequest.class, handler);
    }

    @Override
    public UpdateResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        throw new Exception("This type of request is not supported");
    }
}
