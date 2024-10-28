package pro.gravit.launchserver.base.request.uuid;

import pro.gravit.launchserver.base.events.request.BatchProfileByUsernameRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;
import pro.gravit.launchserver.core.LauncherNetworkAPI;
import pro.gravit.launchserver.utils.helper.IOHelper;

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
