package ru.ricardocraft.backend.auth.core.interfaces.session;

import ru.ricardocraft.backend.auth.core.interfaces.UserHardware;

public interface UserSessionSupportHardware {
    String getHardwareId();
    UserHardware getHardware();
}
