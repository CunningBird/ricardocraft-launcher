package ru.ricardocraft.backend.dto.events.request.secure;

import ru.ricardocraft.backend.dto.events.RequestEvent;

public class SecurityReportRequestEvent extends RequestEvent {
    public final ReportAction action;
    public final String otherAction;

    public SecurityReportRequestEvent() {
        this.action = ReportAction.NONE;
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
