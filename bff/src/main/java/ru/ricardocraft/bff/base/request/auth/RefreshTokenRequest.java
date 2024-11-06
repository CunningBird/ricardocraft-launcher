package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.base.events.request.RefreshTokenRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

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
