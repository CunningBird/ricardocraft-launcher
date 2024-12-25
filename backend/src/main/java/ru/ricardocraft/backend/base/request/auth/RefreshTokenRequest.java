package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.dto.events.request.auth.RefreshTokenRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

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
