package ru.ricardocraft.backend.dto.request.secure;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class SecurityReportRequest extends AbstractRequest {
    public String reportType;
    public String smallData;
    public String largeData;
    public byte[] smallBytes;
    public byte[] largeBytes;
}
