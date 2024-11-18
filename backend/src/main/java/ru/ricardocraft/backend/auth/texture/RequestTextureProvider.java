package ru.ricardocraft.backend.auth.texture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.CommonHelper;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.profiles.Texture;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.TextureProviderProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Primary
public final class RequestTextureProvider extends TextureProvider {

    private final TextureProviderProperties properties;

    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);

    @Autowired
    public RequestTextureProvider(LaunchServerProperties properties) {
        this.properties = properties.getTextureProvider();
    }

    private Texture getTexture(String url, boolean cloak) throws IOException {
        try {
            return new Texture(url, cloak, null);
        } catch (FileNotFoundException ignored) {
            return null; // Simply not found
        }
    }

    private Texture getTexture(String url, Path local, boolean cloak) throws IOException {
        try {
            return new Texture(url, local, cloak, null);
        } catch (FileNotFoundException ignored) {
            return null; // Simply not found
        }
    }

    public String getTextureURL(String url, UUID uuid, String username, String client) {
        return CommonHelper.replace(url, "username", IOHelper.urlEncode(username),
                "uuid", IOHelper.urlEncode(uuid.toString()), "hash", IOHelper.urlEncode(toHash(uuid)),
                "client", IOHelper.urlEncode(client == null ? "unknown" : client));
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) throws IOException {
        String textureUrl = getTextureURL(properties.getCloakURL(), uuid, username, client);
        if (properties.getCloakLocalPath() == null) {
            return getTexture(textureUrl, true);
        } else {
            String path = getTextureURL(properties.getCloakLocalPath(), uuid, username, client);
            return getTexture(textureUrl, Paths.get(path), true);
        }
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) throws IOException {
        String textureUrl = getTextureURL(properties.getSkinURL(), uuid, username, client);
        if (properties.getSkinLocalPath() == null) {
            return getTexture(textureUrl, false);
        } else {
            String path = getTextureURL(properties.getSkinLocalPath(), uuid, username, client);
            return getTexture(textureUrl, Paths.get(path), false);
        }
    }

    private String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }
}
