package pro.gravit.launchserver.base.request.secure;

import pro.gravit.launchserver.base.events.request.GetSecureLevelInfoRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
