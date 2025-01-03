package ru.ricardocraft.backend.service.auth.texture;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.profiles.Texture;

import java.util.UUID;

@Component
public final class VoidTextureProvider extends TextureProvider {

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) {
        return null; // Always nothing
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) {
        return null; // Always nothing
    }
}
