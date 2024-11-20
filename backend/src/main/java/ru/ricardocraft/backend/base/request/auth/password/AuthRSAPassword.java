package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class AuthRSAPassword extends AuthPassword {

    public byte[] password;

    @Override
    public boolean check() {
        return true;
    }
}
