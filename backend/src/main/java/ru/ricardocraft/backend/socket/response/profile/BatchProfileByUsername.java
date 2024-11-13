package ru.ricardocraft.backend.socket.response.profile;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.BatchProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

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
                pair = config.getAuthProviderPair();
            }
            result.playerProfiles[i] = authManager.getPlayerProfile(pair, list[i].username);
        }
        sendResult(result);
    }

    static class Entry {
        String username;
        String client;
    }
}
