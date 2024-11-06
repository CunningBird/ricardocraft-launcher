package ru.ricardocraft.bff.auth.core;

public interface UserSession {
    String getID();

    User getUser();

    String getMinecraftAccessToken();

    long getExpireIn();
}
