package ru.ricardocraft.backend.auth.core.interfaces;

import ru.ricardocraft.backend.base.request.secure.HardwareReportRequest;

public interface UserHardware {
    HardwareReportRequest.HardwareInfo getHardwareInfo();

    byte[] getPublicKey();

    String getId();

    boolean isBanned();
}
