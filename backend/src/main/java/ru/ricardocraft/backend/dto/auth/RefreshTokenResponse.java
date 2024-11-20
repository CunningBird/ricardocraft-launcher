package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class RefreshTokenResponse extends SimpleResponse {
    public String authId;
    public String refreshToken;
}
