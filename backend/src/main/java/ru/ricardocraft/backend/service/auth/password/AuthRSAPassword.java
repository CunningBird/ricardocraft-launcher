package ru.ricardocraft.backend.service.auth.password;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthRSAPassword extends AuthPassword {

    public byte[] password;

    @Override
    public boolean check() {
        return true;
    }
}
