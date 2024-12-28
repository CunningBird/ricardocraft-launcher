package ru.ricardocraft.backend.auth.texture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.profiles.Texture;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.socket.HttpRequester;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JsonTextureProvider extends TextureProvider {

    private static final Map<String, JsonTexture> map = new HashMap<>();

    private transient final HttpRequester requester;
    private transient final RequestTextureProvider requestTextureProvider;
    private transient final LaunchServerProperties config;

    @Autowired
    public JsonTextureProvider(RequestTextureProvider requestTextureProvider,
                               HttpRequester requester,
                               LaunchServerProperties config) {
        this.requester = requester;
        this.requestTextureProvider = requestTextureProvider;
        this.config = config;
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) {
        log.warn("Ineffective get cloak texture for {}", username);
        return getAssets(uuid, username, client).get("CAPE");
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) {
        log.warn("Ineffective get skin texture for {}", username);
        return getAssets(uuid, username, client).get("SKIN");
    }

    @Override
    public Map<String, Texture> getAssets(UUID uuid, String username, String client) {
        try {
            Map<String, JsonTexture> map = requester.send(requester.get(requestTextureProvider.getTextureURL(
                    config.getJsonTextureProvider().getUrl(), uuid, username, client), config.getJsonTextureProvider().getBearerToken()), (Class<Map<String, JsonTexture>>) this.map.getClass()).getOrThrow();
            return JsonTexture.convertMap(map);
        } catch (IOException e) {
            log.error("JsonTextureProvider", e);
            return new HashMap<>();
        }
    }

    public record JsonTexture(String url, String digest, Map<String, String> metadata) {
        public Texture toTexture() {
            return new Texture(url, digest == null ? null : SecurityHelper.fromHex(digest), metadata);
        }

        public static Map<String, Texture> convertMap(Map<String, JsonTexture> map) {
            if (map == null) {
                return new HashMap<>();
            }
            Map<String, Texture> res = new HashMap<>();
            for (var e : map.entrySet()) {
                res.put(e.getKey(), e.getValue().toTexture());
            }
            return res;
        }
    }
}
