package ru.ricardocraft.backend.base.request.uuid;

import ru.ricardocraft.backend.base.events.request.BatchProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.helper.IOHelper;

import java.io.IOException;

public final class BatchProfileByUsernameRequest extends Request<BatchProfileByUsernameRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final Entry[] list;

    public BatchProfileByUsernameRequest(String... usernames) throws IOException {
        this.list = new Entry[usernames.length];
        for (int i = 0; i < usernames.length; ++i) {
            this.list[i] = new Entry();
            this.list[i].client = "";
            this.list[i].username = usernames[i];
        }
        IOHelper.verifyLength(usernames.length, IOHelper.MAX_BATCH_SIZE);
    }

    @Override
    public String getType() {
        return "batchProfileByUsername";
    }

    public static class Entry {
        @LauncherNetworkAPI
        String username;
        @LauncherNetworkAPI
        String client;
    }
}
