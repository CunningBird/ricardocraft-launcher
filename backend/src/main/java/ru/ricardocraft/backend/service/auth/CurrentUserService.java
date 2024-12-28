package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CurrentUserRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class CurrentUserService extends AbstractService {

    private final AuthManager authManager;

    @Autowired
    public CurrentUserService(ServerWebSocketHandler handler, AuthManager authManager) {
        super(CurrentUserRequest.class, handler);
        this.authManager = authManager;
    }

    @Override
    public CurrentUserResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        return new CurrentUserResponse(collectUserInfoFromClient(client));
    }

    public CurrentUserResponse.UserInfo collectUserInfoFromClient(Client client) {
        CurrentUserResponse.UserInfo result = new CurrentUserResponse.UserInfo();
        if (client.auth != null && client.isAuth && client.username != null) {
            result.playerProfile = authManager.getPlayerProfile(client);
        }
        result.permissions = client.permissions;
        return result;
    }
}
