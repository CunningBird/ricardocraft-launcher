package ru.ricardocraft.bff.auth.core.interfaces.session;

import ru.ricardocraft.bff.auth.core.interfaces.UserHardware;

public interface UserSessionSupportHardware {
    String getHardwareId();
    UserHardware getHardware();
}
