package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.RestoreRequest;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;
import ru.ricardocraft.backend.service.controller.auth.RestoreRequestService;

@Component
public class RestoreController extends AbstractController {

    private final RestoreRequestService restoreRequestService;

    public RestoreController(ServerWebSocketHandler handler, RestoreRequestService restoreRequestService) {
        super(RestoreRequest.class, handler);
        this.restoreRequestService = restoreRequestService;
    }

    @Override
    public RestoreResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return restoreRequestService.restoreRequest(castRequest(request), client);
    }
}
