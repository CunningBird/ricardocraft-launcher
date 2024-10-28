package pro.gravit.launchserver.auth.core.interfaces;

import pro.gravit.launchserver.base.request.secure.HardwareReportRequest;

public interface UserHardware {
    HardwareReportRequest.HardwareInfo getHardwareInfo();

    byte[] getPublicKey();

    String getId();

    boolean isBanned();
}
