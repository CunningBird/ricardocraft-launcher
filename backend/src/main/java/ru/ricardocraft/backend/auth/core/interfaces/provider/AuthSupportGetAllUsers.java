package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.Feature;
import ru.ricardocraft.backend.repository.User;

@Feature("users")
public interface AuthSupportGetAllUsers extends AuthSupport {
    Iterable<User> getAllUsers();
}
