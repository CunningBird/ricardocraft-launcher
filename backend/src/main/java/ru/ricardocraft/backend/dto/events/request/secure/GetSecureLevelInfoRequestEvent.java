package ru.ricardocraft.backend.dto.events.request.secure;

import ru.ricardocraft.backend.dto.events.RequestEvent;

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
