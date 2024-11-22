package ru.ricardocraft.backend.auth.protect;

import ru.ricardocraft.backend.dto.socket.auth.AuthResponse;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;

public abstract class ProtectHandler {

    public abstract boolean allowGetAccessToken(AuthResponseService.AuthContext context);

    public boolean allowJoinServer(Client client) {
        return client.isAuth && client.type == AuthResponse.ConnectTypes.CLIENT;
    }
}
