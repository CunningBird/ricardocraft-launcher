package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.UserSession;

public interface AuthSupportExit extends AuthSupport {
    void deleteSession(UserSession session);

    void exitUser(User user);
}
