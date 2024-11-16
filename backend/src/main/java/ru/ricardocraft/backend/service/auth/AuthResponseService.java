package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.manangers.AuthHookManager;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.utils.HookException;

@Component
public class AuthResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    private final AuthManager authManager;

    private final AuthHookManager authHookManager;

    @Autowired
    public AuthResponseService(WebSocketService service,
                                  AuthProviders authProviders,
                                  AuthManager authManager,
                                  AuthHookManager authHookManager) {
        super(AuthResponse.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
        this.authHookManager = authHookManager;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client clientData) throws Exception {
        AuthResponse response = castResponse(rawResponse);

        try {
            AuthRequestEvent result = new AuthRequestEvent();
            AuthProviderPair pair;
            if (response.auth_id == null || response.auth_id.isEmpty()) pair = authProviders.getAuthProviderPair();
            else pair = authProviders.getAuthProviderPair(response.auth_id);
            if (pair == null) {
                sendError(ctx,"auth_id incorrect", response.requestUUID);
                return;
            }
            AuthContext context = authManager.makeAuthContext(clientData, response.authType, pair, response.login, response.client, response.ip);
            authManager.check(context);
            response.password = authManager.decryptPassword(response.password);
            authHookManager.preHook.hook(context, clientData);
            context.report = authManager.auth(context, response.password);
            authHookManager.postHook.hook(context, clientData);
            result.permissions = context.report.session() != null ? (context.report.session().getUser() != null ? context.report.session().getUser().getPermissions() : null) : null;
            if (context.report.isUsingOAuth()) {
                result.oauth = new AuthRequestEvent.OAuthRequestEvent(context.report.oauthAccessToken(), context.report.oauthRefreshToken(), context.report.oauthExpire());
            }
            if (context.report.minecraftAccessToken() != null) {
                result.accessToken = context.report.minecraftAccessToken();
            }
            result.playerProfile = authManager.getPlayerProfile(clientData);
            sendResult(ctx, result, response.requestUUID);
        } catch (AuthException | HookException e) {
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