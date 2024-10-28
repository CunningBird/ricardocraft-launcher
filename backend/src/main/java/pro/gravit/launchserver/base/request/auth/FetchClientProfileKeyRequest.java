package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.FetchClientProfileKeyRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class FetchClientProfileKeyRequest extends Request<FetchClientProfileKeyRequestEvent> {
    public FetchClientProfileKeyRequest() {
    }

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}
