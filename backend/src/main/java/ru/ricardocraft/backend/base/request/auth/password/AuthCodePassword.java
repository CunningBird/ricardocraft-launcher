package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class AuthCodePassword extends AuthPassword {

    public String uri;

    @Override
    public boolean check() {
        return true;
    }
}
