package pro.gravit.launchserver.base.request.management;

import pro.gravit.launchserver.base.events.request.GetConnectUUIDRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
