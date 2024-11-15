package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.events.request.RefreshTokenRequestEvent;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class RefreshTokenResponse extends SimpleResponse {
    public String authId;
    public String refreshToken;

    @Override
    public String getType() {
        return "refreshToken";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (refreshToken == null) {
            sendError("Invalid request");
            return;
        }
        AuthProviderPair pair;
        if (!client.isAuth) {
            if (authId == null) {
                pair = authProviders.getAuthProviderPair();
            } else {
                pair = authProviders.getAuthProviderPair(authId);
            }
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            sendError("Invalid request");
            return;
        }
        AuthManager.AuthReport report = pair.core.refreshAccessToken(refreshToken, new AuthResponse.AuthContext(client, null, null, ip, AuthResponse.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            sendError("Invalid RefreshToken");
            return;
        }
        sendResult(new RefreshTokenRequestEvent(new AuthRequestEvent.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire())));
    }
}
