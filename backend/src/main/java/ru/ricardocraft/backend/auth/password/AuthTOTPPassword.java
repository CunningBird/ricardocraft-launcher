package ru.ricardocraft.backend.auth.password;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthTOTPPassword extends AuthPassword {

    public String totp;

    @Override
    public boolean check() {
        return true;
    }
}
