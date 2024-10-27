package pro.gravit.launcher.gui.base.request.auth;

import pro.gravit.launcher.gui.base.events.request.SetProfileRequestEvent;
import pro.gravit.launcher.gui.base.profiles.ClientProfile;
import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.websockets.WebSocketRequest;
import pro.gravit.launcher.gui.core.LauncherNetworkAPI;

public class SetProfileRequest extends Request<SetProfileRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String client;

    public SetProfileRequest(ClientProfile profile) {
        this.client = profile.getTitle();
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}
