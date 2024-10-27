package pro.gravit.launcher.gui.base.request.secure;

import pro.gravit.launcher.gui.base.events.request.GetSecureLevelInfoRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
