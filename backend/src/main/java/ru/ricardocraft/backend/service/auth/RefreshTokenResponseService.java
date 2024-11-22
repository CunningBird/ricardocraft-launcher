package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.events.request.RefreshTokenRequestEvent;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.dto.socket.auth.AuthResponse;
import ru.ricardocraft.backend.dto.socket.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class RefreshTokenResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    @Autowired
    public RefreshTokenResponseService(WebSocketService service, AuthProviders authProviders) {
        super(RefreshTokenResponse.class, service);
        this.authProviders = authProviders;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        RefreshTokenResponse response = (RefreshTokenResponse) rawResponse;

        if (response.refreshToken == null) {
            sendError(ctx, "Invalid request", response.requestUUID);
            return;
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
            sendError(ctx, "Invalid request", response.requestUUID);
            return;
        }
        AuthManager.AuthReport report = pair.core.refreshAccessToken(response.refreshToken, new AuthResponseService.AuthContext(client, null, null, response.ip, AuthResponse.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            sendError(ctx, "Invalid RefreshToken", response.requestUUID);
            return;
        }
        sendResult(ctx, new RefreshTokenRequestEvent(new AuthRequestEvent.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire())), response.requestUUID);
    }
}
