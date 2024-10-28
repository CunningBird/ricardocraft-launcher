package pro.gravit.launchserver.base.request.update;

import pro.gravit.launchserver.core.LauncherNetworkAPI;
import pro.gravit.launchserver.base.events.request.UpdateRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;

public final class UpdateRequest extends Request<UpdateRequestEvent> implements WebSocketRequest {

    // Instance
    @LauncherNetworkAPI
    public final String dirName;

    public UpdateRequest(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public String getType() {
        return "update";
    }
}
