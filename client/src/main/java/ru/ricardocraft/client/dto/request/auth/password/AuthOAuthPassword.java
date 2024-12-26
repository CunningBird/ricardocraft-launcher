package ru.ricardocraft.client.dto.request.auth.password;

import ru.ricardocraft.client.dto.request.auth.AuthRequest;

public class AuthOAuthPassword implements AuthRequest.AuthPasswordInterface {
    public final String accessToken;
    public final String refreshToken;
    public final int expire;

    public AuthOAuthPassword(String accessToken) {
        this.accessToken = accessToken;
        this.refreshToken = null;
        this.expire = 0;
    }

    @Override
    public boolean check() {
        return true;
    }
}
