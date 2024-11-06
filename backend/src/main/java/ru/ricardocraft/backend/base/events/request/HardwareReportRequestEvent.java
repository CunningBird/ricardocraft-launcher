package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.ExtendedTokenRequestEvent;
import ru.ricardocraft.backend.base.events.RequestEvent;

public class HardwareReportRequestEvent extends RequestEvent implements ExtendedTokenRequestEvent {
    public String extendedToken;
    public long expire;

    public HardwareReportRequestEvent() {
    }

    public HardwareReportRequestEvent(String extendedToken, long expire) {
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
