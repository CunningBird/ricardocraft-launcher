package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class CurrentUserResponse extends SimpleResponse {

    public static CurrentUserRequestEvent.UserInfo collectUserInfoFromClient(LaunchServer server, Client client) {
        CurrentUserRequestEvent.UserInfo result = new CurrentUserRequestEvent.UserInfo();
        if (client.auth != null && client.isAuth && client.username != null) {
            result.playerProfile = server.authManager.getPlayerProfile(client);
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
        sendResult(new CurrentUserRequestEvent(collectUserInfoFromClient(server, client)));
    }
}
