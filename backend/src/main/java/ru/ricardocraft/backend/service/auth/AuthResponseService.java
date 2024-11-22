package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.dto.socket.auth.AuthResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class AuthResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    private final AuthManager authManager;

    private final AuthLimiter authLimiter;

    @Autowired
    public AuthResponseService(WebSocketService service,
                               AuthProviders authProviders,
                               AuthManager authManager,
                               AuthLimiter authLimiter) {
        super(AuthResponse.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.authLimiter = authLimiter;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client clientData) throws Exception {
        AuthResponse response = castResponse(rawResponse);

        try {
            AuthRequestEvent result = new AuthRequestEvent();
            AuthProviderPair pair;
            if (response.auth_id == null || response.auth_id.isEmpty()) pair = authProviders.getAuthProviderPair();
            else pair = authProviders.getAuthProviderPair(response.auth_id);
            if (pair == null) {
                sendError(ctx, "auth_id incorrect", response.requestUUID);
                return;
            }
            AuthContext context = authManager.makeAuthContext(clientData, response.authType, pair, response.login, response.client, response.ip);
            authManager.check(context);
            response.password = authManager.decryptPassword(response.password);
            authLimiter.preAuthHook(context);
            context.report = authManager.auth(context, response.password);
            result.permissions = context.report.session() != null ? (context.report.session().getUser() != null ? context.report.session().getUser().getPermissions() : null) : null;
            if (context.report.isUsingOAuth()) {
                result.oauth = new AuthRequestEvent.OAuthRequestEvent(context.report.oauthAccessToken(), context.report.oauthRefreshToken(), context.report.oauthExpire());
            }
            if (context.report.minecraftAccessToken() != null) {
                result.accessToken = context.report.minecraftAccessToken();
            }
            result.playerProfile = authManager.getPlayerProfile(clientData);
            sendResult(ctx, result, response.requestUUID);
        } catch (AuthException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
        }
    }

    public static class AuthContext {
        public final String login;
        public final String profileName;
        public final String ip;
        public final AuthResponse.ConnectTypes authType;
        public transient final Client client;
        public transient final AuthProviderPair pair;
        public transient AuthManager.AuthReport report;

        public AuthContext(Client client, String login, String profileName, String ip, AuthResponse.ConnectTypes authType, AuthProviderPair pair) {
            this.client = client;
            this.login = login;
            this.profileName = profileName;
            this.ip = ip;
            this.authType = authType;
            this.pair = pair;
        }
    }
}
