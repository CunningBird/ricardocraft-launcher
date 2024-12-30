package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.response.RefreshTokenRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class RefreshTokenRequest extends Request<RefreshTokenRequestEvent> {
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
