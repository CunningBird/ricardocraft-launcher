package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.auth.Feature;
import ru.ricardocraft.bff.auth.core.User;

@Feature("users")
public interface AuthSupportGetAllUsers extends AuthSupport {
    Iterable<User> getAllUsers();
}
