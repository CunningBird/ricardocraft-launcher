package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.repository.User;

public interface AuthSupportExit extends AuthSupport {
    void deleteSession(UserSession session);

    void exitUser(User user);
}
