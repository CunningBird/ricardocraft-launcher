package ru.ricardocraft.backend.service.mirror.modapi;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.ricardocraft.backend.dto.Version;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ModrinthAPI {
    private static final String BASE_URL = "https://api.modrinth.com/v2/";
    private static final String userAgent = "GravitLauncher/%s MirrorHelper/%s".formatted(Version.getVersion().getVersionString(), "1.0.0");

    private final RestClient restClient;

    @SuppressWarnings("unchecked")
    public List<ModVersionData> getMod(String slug) {
        return restClient.get()
                .uri(URI.create(BASE_URL.concat("project/%s/version".formatted(slug))))
                .header("User-Agent", userAgent)
                .retrieve()
                .onStatus(status -> status.value() < 200 || status.value() >= 300, (request, response) -> {
                    throw new IOException("statusCode " + response.getStatusCode());
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public ModVersionData getModByGameVersion(List<ModVersionData> list, String gameVersion, String loader) {
        for (var e : list) {
            if (!e.loaders.contains(loader)) {
                continue;
            }
            if (!e.game_versions.contains(gameVersion)) {
                continue;
            }
            return e;
        }
        return null;
    }

    public record ModVersionData(String id,
                                 String name,
                                 List<ModVersionFileData> files,
                                 List<String> game_versions,
                                 List<String> loaders) {

    }

    public record ModVersionFileData(Map<String, String> hashes, String url, String filename, boolean primary) {

    }
}
