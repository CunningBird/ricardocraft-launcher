package ru.ricardocraft.backend.controller.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class GetConnectUUIDController extends AbstractController {

    private final int shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));

    @Autowired
    public GetConnectUUIDController(ServerWebSocketHandler handler) {
        super(GetConnectUUIDRequest.class, handler);
    }

    @Override
    public GetConnectUUIDResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        GetConnectUUIDRequest response = (GetConnectUUIDRequest) rawResponse;
        return new GetConnectUUIDResponse(response.connectUUID, shardId);
    }
}
