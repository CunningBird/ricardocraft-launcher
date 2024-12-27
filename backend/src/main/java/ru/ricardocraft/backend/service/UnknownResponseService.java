package ru.ricardocraft.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.request.update.UpdateRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.UnknownResponse;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class UnknownResponseService extends AbstractResponseService {

    @Autowired
    public UnknownResponseService(ServerWebSocketHandler handler) {
        super(UnknownResponse.class, handler);
    }

    @Override
    public UpdateRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        throw new Exception("This type of request is not supported");
    }
}
