package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.events.request.RestoreRequestEvent;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.socket.response.auth.RestoreResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RestoreResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;
    private final Map<String, ExtendedTokenProvider> restoreProviders;
    private final CurrentUserResponseService currentUserResponseService;

    @Autowired
    public RestoreResponseService(WebSocketService service,
                                     AuthProviders authProviders,
                                     AuthManager authManager,
                                     Map<String, ExtendedTokenProvider> restoreProviders,
                                     CurrentUserResponseService currentUserResponseService) {
        super(RestoreResponse.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.restoreProviders = restoreProviders;
        this.currentUserResponseService = currentUserResponseService;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        RestoreResponse response = (RestoreResponse) rawResponse;

        if (response.accessToken == null && !client.isAuth && response.needUserInfo) {
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
            sendError(ctx, "Invalid authId", response.requestUUID);
            return;
        }
        if (response.accessToken != null) {
            UserSession session;
            try {
                session = pair.core.getUserSessionByOAuthAccessToken(response.accessToken);
            } catch (AuthCoreProvider.OAuthAccessTokenExpired e) {
                sendError(ctx, AuthRequestEvent.OAUTH_TOKEN_EXPIRE, response.requestUUID);
                return;
            }
            if (session == null) {
                sendError(ctx, AuthRequestEvent.OAUTH_TOKEN_INVALID, response.requestUUID);
                return;
            }
            User user = session.getUser();
            if(user == null) {
                sendError(ctx,"Internal Auth error: UserSession is broken", response.requestUUID);
                return;
            }
            client.coreObject = user;
            client.sessionObject = session;
            authManager.internalAuth(client, client.type == null ? AuthResponse.ConnectTypes.API : client.type, pair, user.getUsername(), user.getUUID(), user.getPermissions(), true);
        }
        List<String> invalidTokens = new ArrayList<>(4);
        if (response.extended != null) {
            response.extended.forEach((k, v) -> {
                ExtendedTokenProvider provider = restoreProviders.get(k);
                if (provider == null) return;
                if (!provider.accept(client, pair, v)) {
                    invalidTokens.add(k);
                }
            });
        }
        if (response.needUserInfo && client.isAuth) {
            sendResult(ctx, new RestoreRequestEvent(currentUserResponseService.collectUserInfoFromClient(client), invalidTokens), response.requestUUID);
        } else {
            sendResult(ctx, new RestoreRequestEvent(invalidTokens), response.requestUUID);
        }
    }

    @FunctionalInterface
    public interface ExtendedTokenProvider {
        boolean accept(Client client, AuthProviderPair pair, String extendedToken);
    }
}