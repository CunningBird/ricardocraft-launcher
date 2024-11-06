package ru.ricardocraft.bff.socket.response.profile;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.BatchProfileByUsernameRequestEvent;
import ru.ricardocraft.bff.base.profiles.PlayerProfile;
import ru.ricardocraft.bff.auth.AuthProviderPair;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

public class BatchProfileByUsername extends SimpleResponse {
    Entry[] list;

    @Override
    public String getType() {
        return "batchProfileByUsername";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        BatchProfileByUsernameRequestEvent result = new BatchProfileByUsernameRequestEvent();
        if (list == null) {
            sendError("Invalid request");
            return;
        }
        result.playerProfiles = new PlayerProfile[list.length];
        for (int i = 0; i < list.length; ++i) {
            AuthProviderPair pair = client.auth;
            if (pair == null) {
                pair = server.config.getAuthProviderPair();
            }
            result.playerProfiles[i] = server.authManager.getPlayerProfile(pair, list[i].username);
        }
        sendResult(result);
    }

    static class Entry {
        String username;
        String client;
    }
}
