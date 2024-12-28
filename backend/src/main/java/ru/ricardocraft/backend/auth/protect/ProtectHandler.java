package ru.ricardocraft.backend.auth.protect;

import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.service.auth.AuthService;
import ru.ricardocraft.backend.socket.Client;

public abstract class ProtectHandler {

    public abstract boolean allowGetAccessToken(AuthService.AuthContext context);

    public boolean allowJoinServer(Client client) {
        return client.isAuth && client.type == AuthRequest.ConnectTypes.CLIENT;
    }
}
