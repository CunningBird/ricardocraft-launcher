package ru.ricardocraft.bff.auth.core;

import ru.ricardocraft.bff.base.ClientPermissions;

import java.util.UUID;

public interface User {
    String getUsername();

    UUID getUUID();

    ClientPermissions getPermissions();

    default boolean isBanned() {
        return false;
    }
}
