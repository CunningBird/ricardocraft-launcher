package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class RefreshTokenRequest extends AbstractRequest {
    public String authId;
    public String refreshToken;
}