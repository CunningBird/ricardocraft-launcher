package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.ExitRequest;
import ru.ricardocraft.backend.dto.response.auth.ExitResponse;
import ru.ricardocraft.backend.service.controller.auth.ExitService;

@Component
public class ExitController extends AbstractController {

    private final ExitService exitService;

    public ExitController(ServerWebSocketHandler handler, ExitService exitService) {
        super(ExitRequest.class, handler);
        this.exitService = exitService;
    }

    @Override
    public ExitResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return exitService.exit(castRequest(request), handler, session, client);
    }
}
