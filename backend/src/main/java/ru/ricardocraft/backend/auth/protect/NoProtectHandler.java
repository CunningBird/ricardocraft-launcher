package ru.ricardocraft.backend.auth.protect;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;

@Component
public class NoProtectHandler extends ProtectHandler {

    @Override
    public boolean allowGetAccessToken(AuthResponseService.AuthContext context) {
        return true;
    }

    @Override
    public boolean allowJoinServer(Client client) {
        return true;
    }
}
