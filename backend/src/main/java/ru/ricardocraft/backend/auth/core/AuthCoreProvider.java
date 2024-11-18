package ru.ricardocraft.backend.auth.core;

import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.base.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/*
All-In-One provider
 */
public abstract class AuthCoreProvider {

    public abstract User getUserByUsername(String username);

    public User getUserByLogin(String login) {
        return getUserByUsername(login);
    }

    public abstract User getUserByUUID(UUID uuid);

    public abstract UserSession getUserSessionByOAuthAccessToken(String accessToken) throws OAuthAccessTokenExpired;

    public abstract AuthManager.AuthReport refreshAccessToken(String refreshToken, AuthResponseService.AuthContext context /* may be null */);

    public void verifyAuth(AuthResponseService.AuthContext context) {
        // None
    }

    public abstract AuthManager.AuthReport authorize(String login, AuthResponseService.AuthContext context /* may be null */, AuthRequest.AuthPasswordInterface password /* may be null */, boolean minecraftAccess) throws IOException;

    public AuthManager.AuthReport authorize(User user, AuthResponseService.AuthContext context /* may be null */, AuthRequest.AuthPasswordInterface password /* may be null */, boolean minecraftAccess) throws IOException {
        return authorize(user.getUsername(), context, password, minecraftAccess);
    }

    public List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getDetails(Client client) {
        return List.of(new AuthPasswordDetails());
    }

    public abstract User checkServer(Client client, String username, String serverID) throws IOException;

    public abstract boolean joinServer(Client client, String username, UUID uuid, String accessToken, String serverID) throws IOException;

    @SuppressWarnings("unchecked")
    public <T> T isSupport(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) return (T) this;
        return null;
    }

    public static class OAuthAccessTokenExpired extends Exception {
        public OAuthAccessTokenExpired() {
        }

        public OAuthAccessTokenExpired(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
