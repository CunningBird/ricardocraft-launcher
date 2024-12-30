package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.AuthLimiter;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;

@Component
@RequiredArgsConstructor
public class AuthRequestService {

    private final AuthProviders authProviders;
    private final AuthService authService;
    private final AuthLimiter authLimiter;

    public AuthResponse getAuth(AuthRequest request, Client clientData) throws Exception {
        try {
            AuthResponse result = new AuthResponse();
            AuthProviderPair pair;
            if (request.auth_id == null || request.auth_id.isEmpty()) pair = authProviders.getAuthProviderPair();
            else pair = authProviders.getAuthProviderPair(request.auth_id);
            if (pair == null) {
                throw new Exception("auth_id incorrect");
            }
            AuthRequestService.AuthContext context = authService.makeAuthContext(clientData, request.authType, pair, request.login, request.client, request.ip);
            authService.check(context);
            request.password = authService.decryptPassword(request.password);
            authLimiter.preAuthHook(context);
            context.report = authService.auth(context, request.password);
            result.permissions = context.report.session() != null ? (context.report.session().getUser() != null ? context.report.session().getUser().getPermissions() : null) : null;
            if (context.report.isUsingOAuth()) {
                result.oauth = new AuthResponse.OAuthRequestEvent(context.report.oauthAccessToken(), context.report.oauthRefreshToken(), context.report.oauthExpire());
            }
            if (context.report.minecraftAccessToken() != null) {
                result.accessToken = context.report.minecraftAccessToken();
            }
            result.playerProfile = authService.getPlayerProfile(clientData);
            return result;
        } catch (AuthException e) {
            throw new Exception(e.getMessage());
        }
    }

    public static class AuthContext {
        public final String login;
        public final String profileName;
        public final String ip;
        public final AuthRequest.ConnectTypes authType;
        public transient final Client client;
        public transient final AuthProviderPair pair;
        public transient AuthService.AuthReport report;

        public AuthContext(Client client, String login, String profileName, String ip, AuthRequest.ConnectTypes authType, AuthProviderPair pair) {
            this.client = client;
            this.login = login;
            this.profileName = profileName;
            this.ip = ip;
            this.authType = authType;
            this.pair = pair;
        }
    }
}
