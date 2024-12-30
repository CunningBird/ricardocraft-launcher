package ru.ricardocraft.backend.service.auth.core.interfaces;

import ru.ricardocraft.backend.dto.request.HardwareReportRequest;

public interface UserHardware {
    HardwareReportRequest.HardwareInfo getHardwareInfo();

    byte[] getPublicKey();

    String getId();

    boolean isBanned();
}
