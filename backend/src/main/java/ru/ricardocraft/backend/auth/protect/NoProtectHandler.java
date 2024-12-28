package ru.ricardocraft.backend.auth.protect;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.AuthService;
import ru.ricardocraft.backend.socket.Client;

@Component
public class NoProtectHandler extends ProtectHandler {

    @Override
    public boolean allowGetAccessToken(AuthService.AuthContext context) {
        return true;
    }

    @Override
    public boolean allowJoinServer(Client client) {
        return true;
    }
}
