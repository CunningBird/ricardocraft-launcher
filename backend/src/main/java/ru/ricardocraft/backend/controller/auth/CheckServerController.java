package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CheckServerRequest;
import ru.ricardocraft.backend.dto.response.auth.CheckServerResponse;
import ru.ricardocraft.backend.service.controller.auth.CheckServerService;

@Component
public class CheckServerController extends AbstractController {

    private final CheckServerService checkServerService;

    public CheckServerController(ServerWebSocketHandler handler, CheckServerService checkServerService) {
        super(CheckServerRequest.class, handler);
        this.checkServerService = checkServerService;
    }

    @Override
    public CheckServerResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return checkServerService.checkServer(castRequest(request), client);
    }
}
