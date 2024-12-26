package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.RefreshTokenRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class RefreshTokenResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    @Autowired
    public RefreshTokenResponseService(ServerWebSocketHandler handler, AuthProviders authProviders) {
        super(RefreshTokenResponse.class, handler);
        this.authProviders = authProviders;
    }

    @Override
    public RefreshTokenRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        RefreshTokenResponse response = (RefreshTokenResponse) rawResponse;

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
        AuthManager.AuthReport report = pair.core.refreshAccessToken(response.refreshToken, new AuthResponseService.AuthContext(client, null, null, response.ip, AuthResponse.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            throw new Exception("Invalid RefreshToken");
        }
        return new RefreshTokenRequestEvent(new AuthRequestEvent.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire()));
    }
}
