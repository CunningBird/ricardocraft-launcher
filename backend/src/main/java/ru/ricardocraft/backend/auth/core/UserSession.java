package ru.ricardocraft.backend.auth.core;

public interface UserSession {
    String getID();

    User getUser();

    String getMinecraftAccessToken();

    long getExpireIn();
}
