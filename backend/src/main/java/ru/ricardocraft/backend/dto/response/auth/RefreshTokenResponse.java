package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class RefreshTokenResponse extends SimpleResponse {
    public String authId;
    public String refreshToken;
}
