package ru.ricardocraft.backend.service.controller.auth;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.RestoreRequest;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.KeyAgreementService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.service.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.service.controller.update.LauncherRequestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestoreRequestService {

    private final Map<String, RestoreRequestService.ExtendedTokenProvider> restoreProviders = new HashMap<>();

    private final AuthProviders authProviders;
    private final AuthService authService;
    private final CurrentUserService currentUserService;

    public RestoreRequestService(AuthProviders authProviders,
                                 AuthService authService,
                                 CurrentUserService currentUserService,
                                 KeyAgreementService keyAgreementService) {
        this.authProviders = authProviders;
        this.authService = authService;
        this.currentUserService = currentUserService;

        restoreProviders.put(LauncherResponse.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherRequestService.LauncherTokenVerifier(keyAgreementService));
        restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementService));
        restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementService));
        restoreProviders.put("checkServer", new AuthService.CheckServerVerifier(authService, authProviders));
    }

    public RestoreResponse restoreRequest(RestoreRequest request, Client client) throws Exception {
        if (request.accessToken == null && !client.isAuth && request.needUserInfo) {
            throw new Exception("Invalid request");
        }
        AuthProviderPair pair;
        if (!client.isAuth) {
            if (request.authId == null) {
                pair = authProviders.getAuthProviderPair();
            } else {
                pair = authProviders.getAuthProviderPair(request.authId);
            }
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("Invalid authId");
        }
        if (request.accessToken != null) {
            UserSession userSession;
            try {
                userSession = pair.core.getUserSessionByOAuthAccessToken(request.accessToken);
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
            authService.internalAuth(client, client.type == null ? AuthRequest.ConnectTypes.API : client.type, pair, user.getUsername(), user.getUUID(), user.getPermissions(), true);
        }
        List<String> invalidTokens = new ArrayList<>(4);
        if (request.extended != null) {
            request.extended.forEach((k, v) -> {
                RestoreRequestService.ExtendedTokenProvider provider = restoreProviders.get(k);
                if (provider == null) return;
                if (!provider.accept(client, pair, v)) {
                    invalidTokens.add(k);
                }
            });
        }
        if (request.needUserInfo && client.isAuth) {
            return new RestoreResponse(currentUserService.collectUserInfoFromClient(client), invalidTokens);
        } else {
            return new RestoreResponse(invalidTokens);
        }
    }

    @FunctionalInterface
    public interface ExtendedTokenProvider {
        boolean accept(Client client, AuthProviderPair pair, String extendedToken);
    }
}
