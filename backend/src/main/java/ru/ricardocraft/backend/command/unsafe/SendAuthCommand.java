package ru.ricardocraft.backend.command.unsafe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;

import java.util.List;
import java.util.UUID;

@Component
public class SendAuthCommand extends Command {

    private transient final NettyServerSocketHandler nettyServerSocketHandler;
    private transient final AuthManager authManager;
    private transient final LaunchServerConfig config;

    @Autowired
    public SendAuthCommand(NettyServerSocketHandler nettyServerSocketHandler,
                           AuthManager authManager,
                           LaunchServerConfig config) {
        super();
        this.nettyServerSocketHandler = nettyServerSocketHandler;
        this.authManager = authManager;
        this.config = config;
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
        AuthProviderPair pair = config.getAuthProviderPair(args[2]);
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
        nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((ch, ws) -> {
            if (!ws.getConnectUUID().equals(connectUUID)) return;
            Client client = ws.getClient();
            client.coreObject = user;
            client.sessionObject = session;
            authManager.internalAuth(client, type, pair, username, uuid, permissions, oauth != null);
            PlayerProfile playerProfile = authManager.getPlayerProfile(client);
            AuthRequestEvent request = new AuthRequestEvent(permissions, playerProfile, minecraftAccessToken, null, null, oauth);
            request.requestUUID = RequestEvent.eventUUID;
            nettyServerSocketHandler.nettyServer.service.sendObject(ch, request);
        });
    }
}