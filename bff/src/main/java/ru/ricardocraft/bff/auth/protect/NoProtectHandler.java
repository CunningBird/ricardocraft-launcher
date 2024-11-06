package ru.ricardocraft.bff.auth.protect;

import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.auth.AuthResponse;

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
