package ru.ricardocraft.backend.socket.response.profile;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.ProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class ProfileByUsername extends SimpleResponse {
    String username;
    String client;

    @Override
    public String getType() {
        return "profileByUsername";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        AuthProviderPair pair = client.auth;
        if (pair == null) pair = server.config.getAuthProviderPair();
        PlayerProfile profile = server.authManager.getPlayerProfile(pair, username);
        if (profile == null) {
            sendError("User not found");
            return;
        }
        sendResult(new ProfileByUsernameRequestEvent(profile));
    }
}
