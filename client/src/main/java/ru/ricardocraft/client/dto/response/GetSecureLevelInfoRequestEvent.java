package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

public class GetSecureLevelInfoRequestEvent extends RequestEvent {
    public final byte[] verifySecureKey;
    public boolean enabled;

    public GetSecureLevelInfoRequestEvent(byte[] verifySecureKey, boolean enabled) {
        this.verifySecureKey = verifySecureKey;
        this.enabled = enabled;
    }

    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
