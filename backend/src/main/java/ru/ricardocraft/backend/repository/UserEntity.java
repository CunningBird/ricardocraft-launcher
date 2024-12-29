package ru.ricardocraft.backend.repository;

import ru.ricardocraft.backend.dto.response.auth.ClientPermissions;

import java.util.UUID;

public record UserEntity(String username, UUID uuid, ClientPermissions permissions) implements User {
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
