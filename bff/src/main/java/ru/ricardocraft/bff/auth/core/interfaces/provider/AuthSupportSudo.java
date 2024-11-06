package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.auth.core.User;
import ru.ricardocraft.bff.manangers.AuthManager;

import java.io.IOException;

public interface AuthSupportSudo {
    AuthManager.AuthReport sudo(User user, boolean shadow) throws IOException;
}
