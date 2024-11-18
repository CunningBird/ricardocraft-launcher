package ru.ricardocraft.backend.dto.secure;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class SecurityReportResponse extends SimpleResponse {
    public String reportType;
    public String smallData;
    public String largeData;
    public byte[] smallBytes;
    public byte[] largeBytes;

    @Override
    public String getType() {
        return "securityReport";
    }
}
