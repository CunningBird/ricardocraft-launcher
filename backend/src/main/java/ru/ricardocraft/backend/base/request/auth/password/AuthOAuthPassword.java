package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class AuthOAuthPassword extends AuthPassword {

    public String accessToken;
    public String refreshToken;
    public int expire;

    @Override
    public boolean check() {
        return true;
    }
}
