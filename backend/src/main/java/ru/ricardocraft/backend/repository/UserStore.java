package ru.ricardocraft.backend.repository;

import java.util.UUID;

public interface UserStore {
    User getByUsername(String username);

    User getUserByUUID(UUID uuid);

    void createOrUpdateUser(User user);
}
