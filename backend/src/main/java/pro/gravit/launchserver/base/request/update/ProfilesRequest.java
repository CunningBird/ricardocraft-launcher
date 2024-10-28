package pro.gravit.launchserver.base.request.update;

import pro.gravit.launchserver.base.events.request.ProfilesRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;

public final class ProfilesRequest extends Request<ProfilesRequestEvent> implements WebSocketRequest {

    @Override
    public String getType() {
        return "profiles";
    }
}
