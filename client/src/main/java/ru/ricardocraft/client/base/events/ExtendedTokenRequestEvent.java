package ru.ricardocraft.client.base.events;

public interface ExtendedTokenRequestEvent {
    String getExtendedTokenName();

    String getExtendedToken();

    long getExtendedTokenExpire();
}
