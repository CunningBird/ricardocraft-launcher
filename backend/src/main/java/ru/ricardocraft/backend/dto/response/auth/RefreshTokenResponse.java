package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;

public class RefreshTokenResponse extends AbstractResponse {
    public AuthResponse.OAuthRequestEvent oauth;

    public RefreshTokenResponse(AuthResponse.OAuthRequestEvent oauth) {
        this.oauth = oauth;
    }

    @Override
    public String getType() {
        return "refreshToken";
    }
}
