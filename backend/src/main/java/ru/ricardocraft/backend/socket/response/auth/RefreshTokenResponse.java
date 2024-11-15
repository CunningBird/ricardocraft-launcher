package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class RefreshTokenResponse extends SimpleResponse {
    public String authId;
    public String refreshToken;

    @Override
    public String getType() {
        return "refreshToken";
    }
}
