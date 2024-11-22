package ru.ricardocraft.backend.auth.password;

import lombok.NoArgsConstructor;

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
