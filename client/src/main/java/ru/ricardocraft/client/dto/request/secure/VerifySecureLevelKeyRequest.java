package ru.ricardocraft.client.dto.request.secure;

import ru.ricardocraft.client.dto.response.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

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
