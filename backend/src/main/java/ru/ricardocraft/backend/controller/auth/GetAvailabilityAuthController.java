package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.service.controller.auth.GetAvailabilityAuthService;

@Component
public class GetAvailabilityAuthController extends AbstractController {

    private final GetAvailabilityAuthService getAvailabilityAuthService;

    public GetAvailabilityAuthController(ServerWebSocketHandler handler, GetAvailabilityAuthService getAvailabilityAuthService) {
        super(GetAvailabilityAuthRequest.class, handler);
        this.getAvailabilityAuthService = getAvailabilityAuthService;
    }

    @Override
    public GetAvailabilityAuthResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return getAvailabilityAuthService.getAvailabilityAuth(client);
    }
}
