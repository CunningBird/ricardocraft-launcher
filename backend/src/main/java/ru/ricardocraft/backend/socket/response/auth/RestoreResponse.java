package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.events.request.RestoreRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestoreResponse extends SimpleResponse {
    public String authId;
    public String accessToken;
    public Map<String, String> extended;
    public boolean needUserInfo;

    @Override
    public String getType() {
        return "restore";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if (accessToken == null && !client.isAuth && needUserInfo) {
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
            sendError("Invalid authId");
            return;
        }
        if (accessToken != null) {
            UserSession session;
            try {
                session = pair.core.getUserSessionByOAuthAccessToken(accessToken);
            } catch (AuthCoreProvider.OAuthAccessTokenExpired e) {
                sendError(AuthRequestEvent.OAUTH_TOKEN_EXPIRE);
                return;
            }
            if (session == null) {
                sendError(AuthRequestEvent.OAUTH_TOKEN_INVALID);
                return;
            }
            User user = session.getUser();
            if(user == null) {
                sendError("Internal Auth error: UserSession is broken");
                return;
            }
            client.coreObject = user;
            client.sessionObject = session;
            authManager.internalAuth(client, client.type == null ? AuthResponse.ConnectTypes.API : client.type, pair, user.getUsername(), user.getUUID(), user.getPermissions(), true);
        }
        List<String> invalidTokens = new ArrayList<>(4);
        if (extended != null) {
            extended.forEach((k, v) -> {
                ExtendedTokenProvider provider = WebSocketService.restoreProviders.get(k);
                if (provider == null) return;
                if (!provider.accept(client, pair, v)) {
                    invalidTokens.add(k);
                }
            });
        }
        if (needUserInfo && client.isAuth) {
            sendResult(new RestoreRequestEvent(CurrentUserResponse.collectUserInfoFromClient(authManager, client), invalidTokens));
        } else {
            sendResult(new RestoreRequestEvent(invalidTokens));
        }
    }

    @FunctionalInterface
    public interface ExtendedTokenProvider {
        boolean accept(Client client, AuthProviderPair pair, String extendedToken);
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
