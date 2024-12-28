package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.RestoreRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.service.update.LauncherService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestoreService extends AbstractService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;
    private final CurrentUserService currentUserResponseService;

    private final Map<String, ExtendedTokenProvider> restoreProviders = new HashMap<>();

    @Autowired
    public RestoreService(ServerWebSocketHandler handler,
                          AuthProviders authProviders,
                          AuthManager authManager,
                          KeyAgreementManager keyAgreementManager,
                          CurrentUserService currentUserResponseService) {
        super(RestoreRequest.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.currentUserResponseService = currentUserResponseService;

        restoreProviders.put(LauncherResponse.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherService.LauncherTokenVerifier(keyAgreementManager));
        restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementManager));
        restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementManager));
        restoreProviders.put("checkServer", new AuthManager.CheckServerVerifier(authManager, authProviders));
    }

    @Override
    public RestoreResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        RestoreRequest response = (RestoreRequest) rawResponse;

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
                throw new Exception(AuthResponse.OAUTH_TOKEN_EXPIRE);
            }
            if (userSession == null) {
                throw new Exception(AuthResponse.OAUTH_TOKEN_INVALID);
            }
            User user = userSession.getUser();
            if (user == null) {
                throw new Exception("Internal Auth error: UserSession is broken");
            }
            client.coreObject = user;
            client.sessionObject = userSession;
            authManager.internalAuth(client, client.type == null ? AuthRequest.ConnectTypes.API : client.type, pair, user.getUsername(), user.getUUID(), user.getPermissions(), true);
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
            return new RestoreResponse(currentUserResponseService.collectUserInfoFromClient(client), invalidTokens);
        } else {
            return new RestoreResponse(invalidTokens);
        }
    }

    @FunctionalInterface
    public interface ExtendedTokenProvider {
        boolean accept(Client client, AuthProviderPair pair, String extendedToken);
    }
}
