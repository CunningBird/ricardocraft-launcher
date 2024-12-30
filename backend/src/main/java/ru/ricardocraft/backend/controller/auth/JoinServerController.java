package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.JoinServerRequest;
import ru.ricardocraft.backend.dto.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.service.controller.auth.JoinServerService;

@Component
public class JoinServerController extends AbstractController {

    private final JoinServerService joinServerService;

    public JoinServerController(ServerWebSocketHandler handler, JoinServerService joinServerService) {
        super(JoinServerRequest.class, handler);
        this.joinServerService = joinServerService;
    }

    @Override
    public JoinServerResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return joinServerService.joinServer(castRequest(request), client);
    }
}
