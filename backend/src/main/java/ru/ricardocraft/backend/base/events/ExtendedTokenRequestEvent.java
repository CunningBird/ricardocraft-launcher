package ru.ricardocraft.backend.base.events;

public interface ExtendedTokenRequestEvent {
    String getExtendedTokenName();

    String getExtendedToken();

    long getExtendedTokenExpire();
}
