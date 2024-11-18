package ru.ricardocraft.backend.auth.texture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.profiles.Texture;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@Primary
public final class RequestTextureProvider extends TextureProvider {
    // Instance
    public String skinURL;
    public String cloakURL;

    public String skinLocalPath;
    public String cloakLocalPath;

    @Autowired
    public RequestTextureProvider(LaunchServerProperties properties) {
        this.skinURL = properties.getTextureProvider().getSkinURL();
        this.cloakURL = properties.getTextureProvider().getCloakURL();
    }

    private static Texture getTexture(String url, boolean cloak) throws IOException {
        try {
            return new Texture(url, cloak, null);
        } catch (FileNotFoundException ignored) {
            return null; // Simply not found
        }
    }

    private static Texture getTexture(String url, Path local, boolean cloak) throws IOException {
        try {
            return new Texture(url, local, cloak, null);
        } catch (FileNotFoundException ignored) {
            return null; // Simply not found
        }
    }

    public static String getTextureURL(String url, UUID uuid, String username, String client) {
        return CommonHelper.replace(url, "username", IOHelper.urlEncode(username),
                "uuid", IOHelper.urlEncode(uuid.toString()), "hash", IOHelper.urlEncode(Launcher.toHash(uuid)),
                "client", IOHelper.urlEncode(client == null ? "unknown" : client));
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) throws IOException {
        String textureUrl = getTextureURL(cloakURL, uuid, username, client);
        if (cloakLocalPath == null) {
            return getTexture(textureUrl, true);
        } else {
            String path = getTextureURL(cloakLocalPath, uuid, username, client);
            return getTexture(textureUrl, Paths.get(path), true);
        }
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) throws IOException {
        String textureUrl = getTextureURL(skinURL, uuid, username, client);
        if (skinLocalPath == null) {
            return getTexture(textureUrl, false);
        } else {
            String path = getTextureURL(skinLocalPath, uuid, username, client);
            return getTexture(textureUrl, Paths.get(path), false);
        }
    }
}
