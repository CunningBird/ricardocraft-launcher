package ru.ricardocraft.backend.command.unsafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.profiles.PlayerProfile;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class SendAuthCommand extends Command {

    private transient final ServerWebSocketHandler serverWebSocketHandler;
    private transient final AuthManager authManager;
    private transient final AuthProviders authProviders;

    @Autowired
    public SendAuthCommand(ServerWebSocketHandler serverWebSocketHandler,
                           AuthManager authManager,
                           AuthProviders authProviders) {
        super();
        this.serverWebSocketHandler = serverWebSocketHandler;
        this.authManager = authManager;
        this.authProviders = authProviders;
    }

    @Override
    public String getArgsDescription() {
        return "[connectUUID] [username] [auth_id] [client type] (permissions)";
    }

    @Override
    public String getUsageDescription() {
        return "manual send auth request";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 4);
        String connectUUID = args[0];
        String username = args[1];
        AuthResponse.ConnectTypes type = AuthResponse.ConnectTypes.valueOf(args[3]);
        AuthProviderPair pair = authProviders.getAuthProviderPair(args[2]);
        ClientPermissions permissions = args.length > 4 ? new ClientPermissions(List.of(), List.of(args[4])) : ClientPermissions.DEFAULT;
        User user = pair.core.getUserByLogin(username);
        UUID uuid;
        if (user == null) {
            uuid = UUID.randomUUID();
        } else {
            uuid = user.getUUID();
        }
        UserSession userSession;
        String minecraftAccessToken;
        AuthRequestEvent.OAuthRequestEvent oauth;
        if (user != null) {
            AuthManager.AuthReport report = pair.core.authorize(user, null, null, true);
            if (report == null) throw new UnsupportedOperationException("AuthCoreProvider not supported sendAuth");
            minecraftAccessToken = report.minecraftAccessToken();

            if (report.isUsingOAuth()) {
                userSession = report.session();
                oauth = new AuthRequestEvent.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire());
            } else {
                userSession = null;
                oauth = null;
            }
        } else {
            userSession = null;
            minecraftAccessToken = null;
            oauth = null;
        }
        serverWebSocketHandler.forEachActiveChannels((session, client) -> {
            if (!session.getId().equals(connectUUID)) return;

            client.coreObject = user;
            client.sessionObject = userSession;
            authManager.internalAuth(client, type, pair, username, uuid, permissions, oauth != null);
            PlayerProfile playerProfile = authManager.getPlayerProfile(client);
            AuthRequestEvent request = new AuthRequestEvent(permissions, playerProfile, minecraftAccessToken, null, null, oauth);
            request.requestUUID = RequestEvent.eventUUID;

            try {
                serverWebSocketHandler.sendMessage(session, request, false);
            } catch (IOException e) {
                log.error("Error occurred during sending message. Cause: {}", e.getMessage());
            }
        });
    }
}
