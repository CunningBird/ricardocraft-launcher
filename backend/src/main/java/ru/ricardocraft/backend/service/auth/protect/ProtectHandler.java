package ru.ricardocraft.backend.service.auth.protect;

import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.controller.Client;

public abstract class ProtectHandler {

    public abstract boolean allowGetAccessToken(AuthController.AuthContext context);

    public boolean allowJoinServer(Client client) {
        return client.isAuth && client.type == AuthRequest.ConnectTypes.CLIENT;
    }
}
