package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.RestoreRequestEvent;
import ru.ricardocraft.backend.dto.events.request.update.LauncherRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.service.update.LauncherResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

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
    public RestoreResponseService(ServerWebSocketHandler handler,
                                  AuthProviders authProviders,
                                  AuthManager authManager,
                                  KeyAgreementManager keyAgreementManager,
                                  CurrentUserResponseService currentUserResponseService) {
        super(RestoreResponse.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.currentUserResponseService = currentUserResponseService;

        restoreProviders.put(LauncherRequestEvent.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherResponseService.LauncherTokenVerifier(keyAgreementManager));
        restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementManager));
        restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementManager));
        restoreProviders.put("checkServer", new AuthManager.CheckServerVerifier(authManager, authProviders));
    }

    @Override
    public RestoreRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        RestoreResponse response = (RestoreResponse) rawResponse;

        if (response.accessToken == null && !client.isAuth && response.needUserInfo) {
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
            throw new Exception("Invalid authId");
        }
        if (response.accessToken != null) {
            UserSession userSession;
            try {
                userSession = pair.core.getUserSessionByOAuthAccessToken(response.accessToken);
            } catch (AuthCoreProvider.OAuthAccessTokenExpired e) {
                throw new Exception(AuthRequestEvent.OAUTH_TOKEN_EXPIRE);
            }
            if (userSession == null) {
                throw new Exception(AuthRequestEvent.OAUTH_TOKEN_INVALID);
            }
            User user = userSession.getUser();
            if (user == null) {
                throw new Exception("Internal Auth error: UserSession is broken");
            }
            client.coreObject = user;
            client.sessionObject = userSession;
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
            return new RestoreRequestEvent(currentUserResponseService.collectUserInfoFromClient(client), invalidTokens);
        } else {
            return new RestoreRequestEvent(invalidTokens);
        }
    }

    @FunctionalInterface
    public interface ExtendedTokenProvider {
        boolean accept(Client client, AuthProviderPair pair, String extendedToken);
    }
}
