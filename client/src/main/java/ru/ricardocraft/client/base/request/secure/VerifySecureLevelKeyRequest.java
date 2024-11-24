package ru.ricardocraft.client.base.request.secure;

import ru.ricardocraft.client.base.events.request.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.client.base.request.Request;

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
