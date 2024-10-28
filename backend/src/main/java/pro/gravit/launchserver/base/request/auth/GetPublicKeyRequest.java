package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.GetPublicKeyRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class GetPublicKeyRequest extends Request<GetPublicKeyRequestEvent> {
    public GetPublicKeyRequest() {
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
