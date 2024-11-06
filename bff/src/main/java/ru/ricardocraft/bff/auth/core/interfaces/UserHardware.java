package ru.ricardocraft.bff.auth.core.interfaces;

import ru.ricardocraft.bff.base.request.secure.HardwareReportRequest;

public interface UserHardware {
    HardwareReportRequest.HardwareInfo getHardwareInfo();

    byte[] getPublicKey();

    String getId();

    boolean isBanned();
}
