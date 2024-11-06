package ru.ricardocraft.bff.auth.core.openid;

import ru.ricardocraft.bff.auth.core.User;

import java.util.UUID;

public interface UserStore {
    User getByUsername(String username);

    User getUserByUUID(UUID uuid);

    void createOrUpdateUser(User user);
}
