package ru.ricardocraft.backend.auth.password;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthCodePassword extends AuthPassword {

    public String uri;

    @Override
    public boolean check() {
        return true;
    }
}
