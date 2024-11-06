package ru.ricardocraft.bff.auth.core;

import ru.ricardocraft.bff.base.request.auth.AuthRequest;
import ru.ricardocraft.bff.auth.AuthException;
import ru.ricardocraft.bff.manangers.AuthManager;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.auth.AuthResponse;

import java.io.IOException;
import java.util.UUID;

public class RejectAuthCoreProvider extends AuthCoreProvider {
    @Override
    public User getUserByUsername(String username) {
        return null;
    }

    @Override
    public User getUserByUUID(UUID uuid) {
        return null;
    }

    @Override
    public UserSession getUserSessionByOAuthAccessToken(String accessToken) {
        return null;
    }

    @Override
    public AuthManager.AuthReport refreshAccessToken(String refreshToken, AuthResponse.AuthContext context) {
        return null;
    }

    @Override
    public void verifyAuth(AuthResponse.AuthContext context) throws AuthException {
        throw new AuthException("Please configure AuthCoreProvider");
    }

    @Override
    public AuthManager.AuthReport authorize(String login, AuthResponse.AuthContext context, AuthRequest.AuthPasswordInterface password, boolean minecraftAccess) throws IOException {
        throw new AuthException("Please configure AuthCoreProvider");
    }

    @Override
    public User checkServer(Client client, String username, String serverID) throws IOException {
        return null;
    }

    @Override
    public boolean joinServer(Client client, String username, UUID uuid, String accessToken, String serverID) throws IOException {
        return false;
    }

    @Override
    public void close() {

    }
}
