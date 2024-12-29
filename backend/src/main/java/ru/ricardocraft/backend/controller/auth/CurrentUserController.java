package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CurrentUserRequest;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class CurrentUserController extends AbstractController {

    private final AuthService authService;

    @Autowired
    public CurrentUserController(ServerWebSocketHandler handler, AuthService authService) {
        super(CurrentUserRequest.class, handler);
        this.authService = authService;
    }

    @Override
    public CurrentUserResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
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
