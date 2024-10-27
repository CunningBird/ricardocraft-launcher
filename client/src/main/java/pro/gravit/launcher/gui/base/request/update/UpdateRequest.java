package pro.gravit.launcher.gui.base.request.update;

import pro.gravit.launcher.gui.base.events.request.UpdateRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.websockets.WebSocketRequest;
import pro.gravit.launcher.gui.core.LauncherNetworkAPI;

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
