package ru.ricardocraft.backend.service.auth.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.ricardocraft.backend.service.auth.details.AuthPasswordDetails;
import ru.ricardocraft.backend.service.auth.password.AuthPassword;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.controller.Client;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public abstract AuthService.AuthReport refreshAccessToken(String refreshToken, AuthController.AuthContext context /* may be null */) throws JsonProcessingException;

    public void verifyAuth(AuthController.AuthContext context) {
        // None
    }

    public abstract AuthService.AuthReport authorize(String login, AuthController.AuthContext context /* may be null */, AuthPassword password /* may be null */, boolean minecraftAccess) throws IOException;

    public AuthService.AuthReport authorize(User user, AuthController.AuthContext context /* may be null */, AuthPassword password /* may be null */, boolean minecraftAccess) throws IOException {
        return authorize(user.getUsername(), context, password, minecraftAccess);
    }

    public List<GetAvailabilityAuthResponse.AuthAvailabilityDetails> getDetails(Client client) {
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

    protected <K, V> V multimapFirstOrNullValue(K key, Map<K, List<V>> params) {
        List<V> list = params.getOrDefault(key, Collections.emptyList());
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    protected Map<String, List<String>> splitUriQuery(URI uri) {
        var query = uri.getRawQuery();
        if (query == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> params = new HashMap<>();
        String[] split = query.split("&");
        for (String qParams : split) {
            String[] splitParams = qParams.split("=");
            List<String> strings = params.computeIfAbsent(URLDecoder.decode(splitParams[0], StandardCharsets.UTF_8),
                    k -> new ArrayList<>(1));
            strings.add(URLDecoder.decode(splitParams[1], StandardCharsets.UTF_8));
        }
        return params;
    }
}
