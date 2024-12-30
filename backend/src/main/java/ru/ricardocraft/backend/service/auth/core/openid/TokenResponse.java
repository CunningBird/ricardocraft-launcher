package ru.ricardocraft.backend.service.auth.core.openid;

public record TokenResponse(String accessToken, long accessTokenExpiresIn,
                            String refreshToken, long refreshTokenExpiresIn) {
}
