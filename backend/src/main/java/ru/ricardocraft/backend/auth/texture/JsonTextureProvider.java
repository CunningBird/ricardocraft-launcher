package ru.ricardocraft.backend.auth.texture;

import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.HttpRequester;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.profiles.Texture;
import ru.ricardocraft.backend.manangers.GsonManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JsonTextureProvider extends TextureProvider {

    private transient final Logger logger = LogManager.getLogger(JsonTextureProvider.class);

    private static final Type MAP_TYPE = new TypeToken<Map<String, JsonTexture>>() {}.getType();

    private transient final HttpRequester requester;
    private transient final RequestTextureProvider requestTextureProvider;
    private transient final LaunchServerConfig.JsonTextureProviderConfig config;

    @Autowired
    public JsonTextureProvider(GsonManager gsonManager,
                               RequestTextureProvider requestTextureProvider,
                               LaunchServerConfig config) {
        this.requester = new HttpRequester(gsonManager);
        this.requestTextureProvider = requestTextureProvider;
        this.config = config.jsonTextureProviderConfig;
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) {
        logger.warn("Ineffective get cloak texture for {}", username);
        return getAssets(uuid, username, client).get("CAPE");
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) {
        logger.warn("Ineffective get skin texture for {}", username);
        return getAssets(uuid, username, client).get("SKIN");
    }

    @Override
    public Map<String, Texture> getAssets(UUID uuid, String username, String client) {
        try {
            Map<String, JsonTexture> map = requester.<Map<String, JsonTexture>>send(requester.get(requestTextureProvider.getTextureURL(config.url, uuid, username, client), config.bearerToken), MAP_TYPE).getOrThrow();
            return JsonTexture.convertMap(map);
        } catch (IOException e) {
            logger.error("JsonTextureProvider", e);
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
            for(var e : map.entrySet()) {
                res.put(e.getKey(), e.getValue().toTexture());
            }
            return res;
        }
    }
}
