package ru.ricardocraft.bff.command.unsafe;

import ru.ricardocraft.bff.base.ClientPermissions;
import ru.ricardocraft.bff.base.events.RequestEvent;
import ru.ricardocraft.bff.base.events.request.AuthRequestEvent;
import ru.ricardocraft.bff.base.profiles.PlayerProfile;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.auth.AuthProviderPair;
import ru.ricardocraft.bff.auth.core.User;
import ru.ricardocraft.bff.auth.core.UserSession;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.manangers.AuthManager;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.auth.AuthResponse;

import java.util.List;
import java.util.UUID;

public class SendAuthCommand extends Command {
    public SendAuthCommand(LaunchServer server) {
        super(server);
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
        UUID connectUUID = parseUUID(args[0]);
        String username = args[1];
        AuthResponse.ConnectTypes type = AuthResponse.ConnectTypes.valueOf(args[3]);
        AuthProviderPair pair = server.config.getAuthProviderPair(args[2]);
        ClientPermissions permissions = args.length > 4 ? new ClientPermissions(List.of(), List.of(args[4])) : ClientPermissions.DEFAULT;
        User user = pair.core.getUserByLogin(username);
        UUID uuid;
        if (user == null) {
            uuid = UUID.randomUUID();
        } else {
            uuid = user.getUUID();
        }
        UserSession session;
        String minecraftAccessToken;
        AuthRequestEvent.OAuthRequestEvent oauth;
        if (user != null) {
            AuthManager.AuthReport report = pair.core.authorize(user, null, null, true);
            if (report == null) throw new UnsupportedOperationException("AuthCoreProvider not supported sendAuth");
            minecraftAccessToken = report.minecraftAccessToken();

            if (report.isUsingOAuth()) {
                session = report.session();
                oauth = new AuthRequestEvent.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire());
            } else {
                session = null;
                oauth = null;
            }
        } else {
            session = null;
            minecraftAccessToken = null;
            oauth = null;
        }
        server.nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((ch, ws) -> {
            if (!ws.getConnectUUID().equals(connectUUID)) return;
            Client client = ws.getClient();
            client.coreObject = user;
            client.sessionObject = session;
            server.authManager.internalAuth(client, type, pair, username, uuid, permissions, oauth != null);
            PlayerProfile playerProfile = server.authManager.getPlayerProfile(client);
            AuthRequestEvent request = new AuthRequestEvent(permissions, playerProfile, minecraftAccessToken, null, null, oauth);
            request.requestUUID = RequestEvent.eventUUID;
            server.nettyServerSocketHandler.nettyServer.service.sendObject(ch, request);
        });
    }
}
