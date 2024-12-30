package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.AbstractResponse;

public class SecurityReportResponse extends AbstractResponse {
    public final ReportAction action;
    public final String otherAction;

    public SecurityReportResponse() {
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
