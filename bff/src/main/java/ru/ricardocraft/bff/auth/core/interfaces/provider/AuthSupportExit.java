package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.auth.core.User;
import ru.ricardocraft.bff.auth.core.UserSession;

public interface AuthSupportExit extends AuthSupport {
    void deleteSession(UserSession session);

    void exitUser(User user);
}
