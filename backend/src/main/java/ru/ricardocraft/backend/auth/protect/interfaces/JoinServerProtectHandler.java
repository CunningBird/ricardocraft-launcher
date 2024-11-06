package ru.ricardocraft.backend.auth.protect.interfaces;

import ru.ricardocraft.backend.socket.Client;

import java.util.UUID;

public interface JoinServerProtectHandler {
    default boolean onJoinServer(String serverID, String username, UUID uuid, Client client) {
        return true;
    }
}
