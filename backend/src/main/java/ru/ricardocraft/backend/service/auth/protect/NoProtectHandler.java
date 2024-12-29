package ru.ricardocraft.backend.service.auth.protect;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.controller.Client;

@Component
public class NoProtectHandler extends ProtectHandler {

    @Override
    public boolean allowGetAccessToken(AuthController.AuthContext context) {
        return true;
    }

    @Override
    public boolean allowJoinServer(Client client) {
        return true;
    }
}
