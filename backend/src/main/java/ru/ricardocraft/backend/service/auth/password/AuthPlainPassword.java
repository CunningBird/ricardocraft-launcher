package ru.ricardocraft.backend.service.auth.password;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthPlainPassword extends AuthPassword {

    public String password;

    public AuthPlainPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean check() {
        return true;
    }
}
