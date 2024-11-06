package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.base.request.auth.AuthRequest;
import ru.ricardocraft.bff.auth.Feature;
import ru.ricardocraft.bff.auth.core.User;

import java.util.Map;

@Feature("registration")
public interface AuthSupportRegistration extends AuthSupport {
    User registration(String login, String email, AuthRequest.AuthPasswordInterface password, Map<String, String> properties);
}
