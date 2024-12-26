package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.events.request.auth.CurrentUserRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class CurrentUserResponseService extends AbstractResponseService {

    private final AuthManager authManager;

    @Autowired
    public CurrentUserResponseService(WebSocketService service, AuthManager authManager) {
        super(CurrentUserResponse.class, service);
        this.authManager = authManager;
    }

    @Override
    public CurrentUserRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
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
