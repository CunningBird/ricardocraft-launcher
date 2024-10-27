package pro.gravit.launcher.gui.base.request.uuid;

import pro.gravit.launcher.gui.core.LauncherNetworkAPI;
import pro.gravit.launcher.gui.base.events.request.BatchProfileByUsernameRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.websockets.WebSocketRequest;
import pro.gravit.launcher.gui.utils.helper.IOHelper;

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
