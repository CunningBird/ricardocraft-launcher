package ru.ricardocraft.backend.auth.core;

import ru.ricardocraft.backend.repository.User;

public interface UserSession {
    String getID();

    User getUser();

    String getMinecraftAccessToken();

    long getExpireIn();
}
