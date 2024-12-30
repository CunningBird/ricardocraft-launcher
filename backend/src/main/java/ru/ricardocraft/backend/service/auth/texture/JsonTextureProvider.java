package ru.ricardocraft.backend.service.auth.texture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.service.profiles.Texture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonTextureProvider extends TextureProvider {

    private final RestClient restClient;
    private final RequestTextureProvider requestTextureProvider;
    private final LaunchServerProperties config;

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
            String textureUrl = requestTextureProvider.getTextureURL(config.getJsonTextureProvider().getUrl(), uuid, username, client);
            Map<String, JsonTexture> map = restClient.get()
                    .uri(textureUrl)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .retrieve()
                    .onStatus(status -> status.value() < 200 || status.value() >= 300, (request, response) -> {
                        throw new IOException("statusCode " + response.getStatusCode());
                    })
                    .body(new ParameterizedTypeReference<>() {});

            return JsonTexture.convertMap(map);
        } catch (Exception e) {
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
