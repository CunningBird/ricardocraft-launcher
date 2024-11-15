package ru.ricardocraft.backend.auth.core;

import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;

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
    public AuthManager.AuthReport refreshAccessToken(String refreshToken, AuthResponseService.AuthContext context) {
        return null;
    }

    @Override
    public void verifyAuth(AuthResponseService.AuthContext context) throws AuthException {
        throw new AuthException("Please configure AuthCoreProvider");
    }

    @Override
    public AuthManager.AuthReport authorize(String login, AuthResponseService.AuthContext context, AuthRequest.AuthPasswordInterface password, boolean minecraftAccess) throws IOException {
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
