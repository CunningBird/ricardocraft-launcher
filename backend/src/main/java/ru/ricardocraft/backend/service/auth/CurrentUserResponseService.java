package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.request.auth.CurrentUserRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class CurrentUserResponseService extends AbstractResponseService {

    private final AuthManager authManager;

    @Autowired
    public CurrentUserResponseService(ServerWebSocketHandler handler, AuthManager authManager) {
        super(CurrentUserResponse.class, handler);
        this.authManager = authManager;
    }

    @Override
    public CurrentUserRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        return new CurrentUserRequestEvent(collectUserInfoFromClient(client));
    }

    public CurrentUserRequestEvent.UserInfo collectUserInfoFromClient(Client client) {
        CurrentUserRequestEvent.UserInfo result = new CurrentUserRequestEvent.UserInfo();
        if (client.auth != null && client.isAuth && client.username != null) {
            result.playerProfile = authManager.getPlayerProfile(client);
        }
        result.permissions = client.permissions;
        return result;
    }
}
