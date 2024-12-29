package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.RefreshTokenRequest;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class RefreshTokenController extends AbstractController {

    private final AuthProviders authProviders;

    @Autowired
    public RefreshTokenController(ServerWebSocketHandler handler, AuthProviders authProviders) {
        super(RefreshTokenRequest.class, handler);
        this.authProviders = authProviders;
    }

    @Override
    public RefreshTokenResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        RefreshTokenRequest response = (RefreshTokenRequest) rawResponse;

        if (response.refreshToken == null) {
            throw new Exception("Invalid request");
        }
        AuthProviderPair pair;
        if (!client.isAuth) {
            if (response.authId == null) {
                pair = authProviders.getAuthProviderPair();
            } else {
                pair = authProviders.getAuthProviderPair(response.authId);
            }
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("Invalid request");
        }
        AuthService.AuthReport report = pair.core.refreshAccessToken(response.refreshToken, new AuthController.AuthContext(client, null, null, response.ip, AuthRequest.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            throw new Exception("Invalid RefreshToken");
        }
        return new RefreshTokenResponse(new AuthResponse.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire()));
    }
}
