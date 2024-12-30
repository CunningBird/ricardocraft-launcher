package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.RefreshTokenRequest;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.service.controller.auth.RefreshTokenService;

@Component
public class RefreshTokenController extends AbstractController {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenController(ServerWebSocketHandler handler, RefreshTokenService refreshTokenService) {
        super(RefreshTokenRequest.class, handler);
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public RefreshTokenResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return refreshTokenService.refreshToken(castRequest(request), client);
    }
}
