package ru.ricardocraft.client.base.request.auth.password;

import ru.ricardocraft.client.base.request.auth.AuthRequest;

public class Auth2FAPassword implements AuthRequest.AuthPasswordInterface {
    public AuthRequest.AuthPasswordInterface firstPassword;
    public AuthRequest.AuthPasswordInterface secondPassword;

    @Override
    public boolean check() {
        return firstPassword != null && firstPassword.check() && secondPassword != null && secondPassword.check();
    }

    @Override
    public boolean isAllowSave() {
        return firstPassword.isAllowSave() && secondPassword.isAllowSave();
    }
}