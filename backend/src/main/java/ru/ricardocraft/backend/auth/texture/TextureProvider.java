package ru.ricardocraft.backend.auth.texture;

import ru.ricardocraft.backend.base.profiles.Texture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class TextureProvider {

    public abstract Texture getCloakTexture(UUID uuid, String username, String client) throws IOException;


    public abstract Texture getSkinTexture(UUID uuid, String username, String client) throws IOException;

    @Deprecated
    public SkinAndCloakTextures getTextures(UUID uuid, String username, String client) {

        Texture skin;
        try {
            skin = getSkinTexture(uuid, username, client);
        } catch (IOException e) {
            skin = null;
        }

        // Get cloak texture
        Texture cloak;
        try {
            cloak = getCloakTexture(uuid, username, client);
        } catch (IOException e) {
            cloak = null;
        }

        return new SkinAndCloakTextures(skin, cloak);
    }

    public Map<String, Texture> getAssets(UUID uuid, String username, String client) {

        Texture skin;
        try {
            skin = getSkinTexture(uuid, username, client);
        } catch (IOException e) {
            skin = null;
        }

        // Get cloak texture
        Texture cloak;
        try {
            cloak = getCloakTexture(uuid, username, client);
        } catch (IOException e) {
            cloak = null;
        }

        Map<String, Texture> map = new HashMap<>();
        if (skin != null) {
            map.put("SKIN", skin);
        }
        if (cloak != null) {
            map.put("CAPE", cloak);
        }

        return map;
    }

    @Deprecated
    public static class SkinAndCloakTextures {
        public final Texture skin;
        public final Texture cloak;

        public SkinAndCloakTextures(Texture skin, Texture cloak) {
            this.skin = skin;
            this.cloak = cloak;
        }
    }
}
