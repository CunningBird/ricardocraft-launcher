package ru.ricardocraft.backend.auth.protect;

import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;

public class NoProtectHandler extends ProtectHandler {

    @Override
    public boolean allowGetAccessToken(AuthResponse.AuthContext context) {
        return true;
    }

    @Override
    public boolean allowJoinServer(Client client) {
        return true;
    }
}
