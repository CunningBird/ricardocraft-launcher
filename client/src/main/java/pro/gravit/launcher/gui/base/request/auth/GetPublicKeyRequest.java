package pro.gravit.launcher.gui.base.request.auth;

import pro.gravit.launcher.gui.base.events.request.GetPublicKeyRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class GetPublicKeyRequest extends Request<GetPublicKeyRequestEvent> {
    public GetPublicKeyRequest() {
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
