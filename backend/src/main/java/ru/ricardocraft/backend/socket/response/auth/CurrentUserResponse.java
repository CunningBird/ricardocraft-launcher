package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class CurrentUserResponse extends SimpleResponse {

    public static CurrentUserRequestEvent.UserInfo collectUserInfoFromClient(AuthManager authManager, Client client) {
        CurrentUserRequestEvent.UserInfo result = new CurrentUserRequestEvent.UserInfo();
        if (client.auth != null && client.isAuth && client.username != null) {
            result.playerProfile = authManager.getPlayerProfile(client);
        }
        result.permissions = client.permissions;
        return result;
    }

    @Override
    public String getType() {
        return "currentUser";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        sendResult(new CurrentUserRequestEvent(collectUserInfoFromClient(authManager, client)));
    }
}
