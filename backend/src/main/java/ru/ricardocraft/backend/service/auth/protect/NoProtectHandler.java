package ru.ricardocraft.backend.service.auth.protect;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.service.controller.auth.AuthRequestService;

@Component
public class NoProtectHandler extends ProtectHandler {

    @Override
    public boolean allowGetAccessToken(AuthRequestService.AuthContext context) {
        return true;
    }

    @Override
    public boolean allowJoinServer(Client client) {
        return true;
    }
}
