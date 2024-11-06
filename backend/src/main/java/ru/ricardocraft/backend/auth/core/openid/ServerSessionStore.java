package ru.ricardocraft.backend.auth.core.openid;

import java.util.UUID;

public interface ServerSessionStore {
    boolean joinServer(UUID uuid, String username, String serverId);
    String getServerIdByUsername(String username);
}
