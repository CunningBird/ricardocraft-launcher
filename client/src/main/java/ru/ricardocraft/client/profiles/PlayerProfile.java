package ru.ricardocraft.client.profiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PlayerProfile {

    public final UUID uuid;
    public final String username;
    public final Map<String, Texture> assets;
    public final Map<String, String> properties;


    @Deprecated
    public PlayerProfile(UUID uuid, String username, Texture skin, Texture cloak) {
        this(uuid, username, skin, cloak, new HashMap<>());
    }

    @Deprecated
    public PlayerProfile(UUID uuid, String username, Texture skin, Texture cloak, Map<String, String> properties) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.username = username;
        this.assets = new HashMap<>();
        if (skin != null) {
            this.assets.put("SKIN", skin);
        }
        if (cloak != null) {
            this.assets.put("CAPE", cloak);
        }
        this.properties = properties;
    }

    public PlayerProfile(UUID uuid, String username, Map<String, Texture> assets, Map<String, String> properties) {
        this.uuid = uuid;
        this.username = username;
        this.assets = assets;
        this.properties = properties;
    }

}
