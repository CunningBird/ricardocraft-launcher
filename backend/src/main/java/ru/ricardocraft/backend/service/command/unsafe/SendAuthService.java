package ru.ricardocraft.backend.service.command.unsafe;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.ClientPermissions;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendAuthService {

    private final ServerWebSocketHandler serverWebSocketHandler;
    private final AuthService authService;
    private final AuthProviders authProviders;

    public void cipherList(String connectUUID, String username, String auth_id, String clientType, @Nullable String[] cipherPermissions) throws Exception {
        AuthRequest.ConnectTypes type = AuthRequest.ConnectTypes.valueOf(clientType);
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
        AuthResponse.OAuthRequestEvent oauth;
        if (user != null) {
            AuthService.AuthReport report = pair.core.authorize(user, null, null, true);
            if (report == null) throw new UnsupportedOperationException("AuthCoreProvider not supported sendAuth");
            minecraftAccessToken = report.minecraftAccessToken();

            if (report.isUsingOAuth()) {
                userSession = report.session();
                oauth = new AuthResponse.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire());
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
            authService.internalAuth(client, type, pair, username, uuid, permissions, oauth != null);
            PlayerProfile playerProfile = authService.getPlayerProfile(client);
            AuthResponse request = new AuthResponse(permissions, playerProfile, minecraftAccessToken, null, null, oauth);
            request.requestUUID = AbstractResponse.eventUUID;

            try {
                serverWebSocketHandler.sendMessage(session, request, false);
            } catch (IOException e) {
                log.error("Error occurred during sending message. Cause: {}", e.getMessage());
            }
        });
    }
}
