package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class AuthService extends AbstractService {

    private final AuthProviders authProviders;

    private final AuthManager authManager;

    private final AuthLimiter authLimiter;

    @Autowired
    public AuthService(ServerWebSocketHandler handler,
                       AuthProviders authProviders,
                       AuthManager authManager,
                       AuthLimiter authLimiter) {
        super(AuthRequest.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.authLimiter = authLimiter;
    }

    @Override
    public AuthResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client clientData) throws Exception {
        AuthRequest response = castResponse(rawResponse);

        try {
            AuthResponse result = new AuthResponse();
            AuthProviderPair pair;
            if (response.auth_id == null || response.auth_id.isEmpty()) pair = authProviders.getAuthProviderPair();
            else pair = authProviders.getAuthProviderPair(response.auth_id);
            if (pair == null) {
                throw new Exception("auth_id incorrect");
            }
            AuthContext context = authManager.makeAuthContext(clientData, response.authType, pair, response.login, response.client, response.ip);
            authManager.check(context);
            response.password = authManager.decryptPassword(response.password);
            authLimiter.preAuthHook(context);
            context.report = authManager.auth(context, response.password);
            result.permissions = context.report.session() != null ? (context.report.session().getUser() != null ? context.report.session().getUser().getPermissions() : null) : null;
            if (context.report.isUsingOAuth()) {
                result.oauth = new AuthResponse.OAuthRequestEvent(context.report.oauthAccessToken(), context.report.oauthRefreshToken(), context.report.oauthExpire());
            }
            if (context.report.minecraftAccessToken() != null) {
                result.accessToken = context.report.minecraftAccessToken();
            }
            result.playerProfile = authManager.getPlayerProfile(clientData);
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
        public transient AuthManager.AuthReport report;

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
