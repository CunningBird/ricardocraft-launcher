package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.CurrentUserResponse;

@Component
public class CurrentUserResponseService extends AbstractResponseService {

    private final AuthManager authManager;

    @Autowired
    public CurrentUserResponseService(WebSocketService service, AuthManager authManager) {
        super(CurrentUserResponse.class, service);
        this.authManager = authManager;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        CurrentUserResponse response = (CurrentUserResponse) rawResponse;
        sendResult(ctx, new CurrentUserRequestEvent(collectUserInfoFromClient(client)), response.requestUUID);
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
