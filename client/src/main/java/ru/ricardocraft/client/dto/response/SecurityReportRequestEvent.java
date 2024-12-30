package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

public class SecurityReportRequestEvent extends RequestEvent {
    public final ReportAction action;
    public final String otherAction;

    public SecurityReportRequestEvent(ReportAction action) {
        this.action = action;
        this.otherAction = null;
    }

    @Override
    public String getType() {
        return "securityReport";
    }

    public enum ReportAction {
        NONE,
        LOGOUT,
        TOKEN_EXPIRED,
        EXIT,
        @Deprecated
        CRASH,
        OTHER
    }
}
