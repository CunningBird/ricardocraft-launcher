package ru.ricardocraft.backend.service.auth.core;

import ru.ricardocraft.backend.repository.User;

public interface UserSession {
    String getID();

    User getUser();

    String getMinecraftAccessToken();

    long getExpireIn();
}
