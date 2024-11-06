package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.manangers.AuthManager;

import java.io.IOException;

public interface AuthSupportSudo {
    AuthManager.AuthReport sudo(User user, boolean shadow) throws IOException;
}
