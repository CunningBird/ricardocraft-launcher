package ru.ricardocraft.client.dto;

public interface ExtendedTokenRequestEvent {
    String getExtendedTokenName();

    String getExtendedToken();

    long getExtendedTokenExpire();
}
