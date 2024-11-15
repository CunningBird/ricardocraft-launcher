package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;

public class GetSecureLevelInfoRequestEvent extends RequestEvent {
    public final byte[] verifySecureKey;
    public boolean enabled;

    public GetSecureLevelInfoRequestEvent(byte[] verifySecureKey) {
        this.verifySecureKey = verifySecureKey;
    }

    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
