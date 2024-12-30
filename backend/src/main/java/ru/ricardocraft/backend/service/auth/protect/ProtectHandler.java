package ru.ricardocraft.backend.service.auth.protect;

import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.service.controller.auth.AuthRequestService;

public abstract class ProtectHandler {

    public abstract boolean allowGetAccessToken(AuthRequestService.AuthContext context);

    public boolean allowJoinServer(Client client) {
        return client.isAuth && client.type == AuthRequest.ConnectTypes.CLIENT;
    }
}
