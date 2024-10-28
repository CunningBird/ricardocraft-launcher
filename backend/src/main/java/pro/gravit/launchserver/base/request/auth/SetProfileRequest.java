package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.SetProfileRequestEvent;
import pro.gravit.launchserver.base.profiles.ClientProfile;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;
import pro.gravit.launchserver.core.LauncherNetworkAPI;

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
