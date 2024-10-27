package pro.gravit.launcher.gui.base.request.auth;

import pro.gravit.launcher.gui.base.events.request.FetchClientProfileKeyRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class FetchClientProfileKeyRequest extends Request<FetchClientProfileKeyRequestEvent> {
    public FetchClientProfileKeyRequest() {
    }

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}
