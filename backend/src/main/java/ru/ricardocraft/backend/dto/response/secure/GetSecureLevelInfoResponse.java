package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.AbstractResponse;

public class GetSecureLevelInfoResponse extends AbstractResponse {
    public final byte[] verifySecureKey;
    public boolean enabled;

    public GetSecureLevelInfoResponse(byte[] verifySecureKey) {
        this.verifySecureKey = verifySecureKey;
    }

    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
