package pro.gravit.launcher.gui.base.request.uuid;

import pro.gravit.launcher.gui.base.events.request.ProfileByUsernameRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.websockets.WebSocketRequest;
import pro.gravit.launcher.gui.core.LauncherNetworkAPI;

public final class ProfileByUsernameRequest extends Request<ProfileByUsernameRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String username;


    public ProfileByUsernameRequest(String username) {
        this.username = username;
    }

    @Override
    public String getType() {
        return "profileByUsername";
    }
}
