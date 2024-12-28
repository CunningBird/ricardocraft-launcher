package ru.ricardocraft.backend.command.unsafe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.ServerWebSocketHandler;
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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@ShellComponent
@ShellCommandGroup("unsafe")
@RequiredArgsConstructor
public class SendAuthCommand {

    private final ServerWebSocketHandler serverWebSocketHandler;
    private final AuthManager authManager;
    private final AuthProviders authProviders;

    @ShellMethod("[connectUUID] [username] [auth_id] [client type] (permissions) manual send auth request")
    public void cipherList(@ShellOption String connectUUID,
                           @ShellOption String username,
                           @ShellOption String auth_id,
                           @ShellOption String clientType,
                           @ShellOption(defaultValue = ShellOption.NULL) String[] cipherPermissions) throws Exception {
        AuthResponse.ConnectTypes type = AuthResponse.ConnectTypes.valueOf(clientType);
        AuthProviderPair pair = authProviders.getAuthProviderPair(auth_id);
        ClientPermissions permissions = cipherPermissions != null ? new ClientPermissions(List.of(), List.of(cipherPermissions)) : ClientPermissions.DEFAULT;
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
