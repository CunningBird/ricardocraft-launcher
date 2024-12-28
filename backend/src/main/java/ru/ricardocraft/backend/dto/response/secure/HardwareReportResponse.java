package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.ExtendedTokenResponse;
import ru.ricardocraft.backend.dto.AbstractResponse;

public class HardwareReportResponse extends AbstractResponse implements ExtendedTokenResponse {
    public String extendedToken;
    public long expire;

    public HardwareReportResponse() {
    }

    public HardwareReportResponse(String extendedToken, long expire) {
        this.extendedToken = extendedToken;
        this.expire = expire;
    }

    @Override
    public String getType() {
        return "hardwareReport";
    }

    @Override
    public String getExtendedTokenName() {
        return "hardware";
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
