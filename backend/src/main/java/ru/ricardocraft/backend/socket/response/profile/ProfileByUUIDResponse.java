package ru.ricardocraft.backend.socket.response.profile;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.ProfileByUUIDRequestEvent;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;

    @Override
    public String getType() {
        return "profileByUUID";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        AuthProviderPair pair;
        if (client.auth == null) {
            pair = config.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            sendError("ProfileByUUIDResponse: AuthProviderPair is null");
            return;
        }
        User user = pair.core.getUserByUUID(uuid);
        if (user == null) {
            sendError("User not found");
            return;
        }
        sendResult(new ProfileByUUIDRequestEvent(authManager.getPlayerProfile(pair, uuid)));
    }
}