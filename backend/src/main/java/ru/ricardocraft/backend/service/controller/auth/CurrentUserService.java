package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.service.AuthService;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final AuthService authService;

    public CurrentUserResponse getCurrentUser(Client client) {
        return new CurrentUserResponse(collectUserInfoFromClient(client));
    }

    public CurrentUserResponse.UserInfo collectUserInfoFromClient(Client client) {
        CurrentUserResponse.UserInfo result = new CurrentUserResponse.UserInfo();
        if (client.auth != null && client.isAuth && client.username != null) {
            result.playerProfile = authService.getPlayerProfile(client);
        }
        result.permissions = client.permissions;
        return result;
    }
}
