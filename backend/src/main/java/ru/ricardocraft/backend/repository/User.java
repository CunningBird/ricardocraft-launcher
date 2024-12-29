package ru.ricardocraft.backend.repository;

import ru.ricardocraft.backend.dto.response.auth.ClientPermissions;

import java.util.UUID;

public interface User {
    String getUsername();

    UUID getUUID();

    ClientPermissions getPermissions();

    default boolean isBanned() {
        return false;
    }
}
