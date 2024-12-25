package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;
import ru.ricardocraft.backend.dto.events.request.update.LauncherRequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.RestoreRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.service.update.LauncherResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestoreResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;
    private final CurrentUserResponseService currentUserResponseService;

    private final Map<String, ExtendedTokenProvider> restoreProviders = new HashMap<>();

    @Autowired
    public RestoreResponseService(WebSocketService service,
                                  AuthProviders authProviders,
                                  AuthManager authManager,
                                  KeyAgreementManager keyAgreementManager,
                                  CurrentUserResponseService currentUserResponseService) {
        super(RestoreResponse.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.currentUserResponseService = currentUserResponseService;

        restoreProviders.put(LauncherRequestEvent.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherResponseService.LauncherTokenVerifier(keyAgreementManager));
        restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementManager));
        restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementManager));
        restoreProviders.put("checkServer", new AuthManager.CheckServerVerifier(authManager, authProviders));
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
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
            if (user == null) {
                sendError(ctx, "Internal Auth error: UserSession is broken", response.requestUUID);
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
