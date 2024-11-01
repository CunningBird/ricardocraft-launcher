package pro.gravit.launcher.gui.base.request.secure;

import pro.gravit.launcher.gui.base.events.request.VerifySecureLevelKeyRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class VerifySecureLevelKeyRequest extends Request<VerifySecureLevelKeyRequestEvent> {
    public final byte[] publicKey;
    public final byte[] signature;

    public VerifySecureLevelKeyRequest(byte[] publicKey, byte[] signature) {
        this.publicKey = publicKey;
        this.signature = signature;
    }

    @Override
    public String getType() {
        return "verifySecureLevelKey";
    }
}
