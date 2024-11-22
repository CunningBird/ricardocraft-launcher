package ru.ricardocraft.backend.dto.socket.secure;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class SecurityReportResponse extends SimpleResponse {
    public String reportType;
    public String smallData;
    public String largeData;
    public byte[] smallBytes;
    public byte[] largeBytes;
}
