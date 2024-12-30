package ru.ricardocraft.backend.dto;

public interface ExtendedTokenResponse {
    String getExtendedTokenName();

    String getExtendedToken();

    long getExtendedTokenExpire();
}
