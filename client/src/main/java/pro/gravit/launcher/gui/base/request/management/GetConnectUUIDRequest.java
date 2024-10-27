package pro.gravit.launcher.gui.base.request.management;

import pro.gravit.launcher.gui.base.events.request.GetConnectUUIDRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
