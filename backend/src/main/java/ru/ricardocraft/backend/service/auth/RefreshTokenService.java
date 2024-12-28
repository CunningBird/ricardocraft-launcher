package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.RefreshTokenRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class RefreshTokenService extends AbstractService {

    private final AuthProviders authProviders;

    @Autowired
    public RefreshTokenService(ServerWebSocketHandler handler, AuthProviders authProviders) {
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
        AuthManager.AuthReport report = pair.core.refreshAccessToken(response.refreshToken, new AuthService.AuthContext(client, null, null, response.ip, AuthRequest.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            throw new Exception("Invalid RefreshToken");
        }
        return new RefreshTokenResponse(new AuthResponse.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire()));
    }
}
