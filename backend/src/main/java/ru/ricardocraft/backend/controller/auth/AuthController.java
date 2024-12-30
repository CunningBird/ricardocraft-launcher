package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.service.controller.auth.AuthRequestService;

@Component
public class AuthController extends AbstractController {

    private final AuthRequestService authRequestService;

    public AuthController(ServerWebSocketHandler handler, AuthRequestService authRequestService) {
        super(AuthRequest.class, handler);
        this.authRequestService = authRequestService;
    }

    @Override
    public AuthResponse execute(AbstractRequest request, WebSocketSession session, Client clientData) throws Exception {
        return authRequestService.getAuth(castRequest(request), clientData);
    }
}
