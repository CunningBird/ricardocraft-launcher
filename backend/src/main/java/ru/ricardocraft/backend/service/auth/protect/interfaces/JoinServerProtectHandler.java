package ru.ricardocraft.backend.service.auth.protect.interfaces;

import ru.ricardocraft.backend.controller.Client;

import java.util.UUID;

public interface JoinServerProtectHandler {
    default boolean onJoinServer(String serverID, String username, UUID uuid, Client client) {
        return true;
    }
}
