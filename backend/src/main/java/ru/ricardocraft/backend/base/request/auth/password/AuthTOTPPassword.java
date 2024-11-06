package ru.ricardocraft.backend.base.request.auth.password;

import ru.ricardocraft.backend.base.request.auth.AuthRequest;

public class AuthTOTPPassword implements AuthRequest.AuthPasswordInterface {
    public String totp;

    @Override
    public boolean check() {
        return true;
    }
}
