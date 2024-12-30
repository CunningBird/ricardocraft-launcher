package ru.ricardocraft.backend.service.auth.core.interfaces.session;

import ru.ricardocraft.backend.service.auth.core.interfaces.UserHardware;

public interface UserSessionSupportHardware {
    String getHardwareId();
    UserHardware getHardware();
}
