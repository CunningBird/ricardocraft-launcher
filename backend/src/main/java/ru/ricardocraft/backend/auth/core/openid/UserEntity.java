package ru.ricardocraft.backend.auth.core.openid;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.auth.core.User;

import java.util.UUID;

record UserEntity(String username, UUID uuid, ClientPermissions permissions) implements User {
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public ClientPermissions getPermissions() {
        return permissions;
    }
}
