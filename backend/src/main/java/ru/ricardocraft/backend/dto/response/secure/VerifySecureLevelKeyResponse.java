package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.ExtendedTokenResponse;
import ru.ricardocraft.backend.dto.AbstractResponse;

public class VerifySecureLevelKeyResponse extends AbstractResponse implements ExtendedTokenResponse {
    public boolean needHardwareInfo;
    public boolean onlyStatisticInfo;
    public String extendedToken;
    public long expire;

    public VerifySecureLevelKeyResponse() {
    }

    public VerifySecureLevelKeyResponse(boolean needHardwareInfo, boolean onlyStatisticInfo, String extendedToken, long expire) {
        this.needHardwareInfo = needHardwareInfo;
        this.onlyStatisticInfo = onlyStatisticInfo;
        this.extendedToken = extendedToken;
        this.expire = expire;
    }

    @Override
    public String getType() {
        return "verifySecureLevelKey";
    }

    @Override
    public String getExtendedTokenName() {
        return "publicKey";
    }

    @Override
    public String getExtendedToken() {
        return extendedToken;
    }

    @Override
    public long getExtendedTokenExpire() {
        return expire;
    }
}
