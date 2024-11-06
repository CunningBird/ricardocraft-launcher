package ru.ricardocraft.backend.auth.core.openid;

import ru.ricardocraft.backend.auth.core.User;

import java.util.UUID;

public interface UserStore {
    User getByUsername(String username);

    User getUserByUUID(UUID uuid);

    void createOrUpdateUser(User user);
}
