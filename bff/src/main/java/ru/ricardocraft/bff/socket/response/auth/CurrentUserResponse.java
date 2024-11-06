package ru.ricardocraft.bff.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

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
