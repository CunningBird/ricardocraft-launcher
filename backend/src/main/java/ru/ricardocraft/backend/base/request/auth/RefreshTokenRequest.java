package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;

public class RefreshTokenRequest extends Request<RefreshTokenResponse> {
    public String authId;
    public String refreshToken;

    public RefreshTokenRequest(String authId, String refreshToken) {
        this.authId = authId;
        this.refreshToken = refreshToken;
    }

    @Override
    public String getType() {
        return "refreshToken";
    }
}
