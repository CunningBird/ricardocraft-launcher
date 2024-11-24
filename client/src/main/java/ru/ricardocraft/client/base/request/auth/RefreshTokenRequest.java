package ru.ricardocraft.client.base.request.auth;

import ru.ricardocraft.client.base.events.request.RefreshTokenRequestEvent;
import ru.ricardocraft.client.base.request.Request;

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