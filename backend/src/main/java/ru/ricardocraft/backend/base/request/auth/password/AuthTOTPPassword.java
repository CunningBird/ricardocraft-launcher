package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class AuthTOTPPassword extends AuthPassword {

    public String totp;

    @Override
    public boolean check() {
        return true;
    }
}
