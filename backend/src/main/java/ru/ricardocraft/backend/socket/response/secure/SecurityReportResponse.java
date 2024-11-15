package ru.ricardocraft.backend.socket.response.secure;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

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
