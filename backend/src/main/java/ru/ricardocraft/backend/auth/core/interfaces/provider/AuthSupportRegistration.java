package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.Feature;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;

import java.util.Map;

@Feature("registration")
public interface AuthSupportRegistration extends AuthSupport {
    User registration(String login, String email, AuthRequest.AuthPasswordInterface password, Map<String, String> properties);
}
