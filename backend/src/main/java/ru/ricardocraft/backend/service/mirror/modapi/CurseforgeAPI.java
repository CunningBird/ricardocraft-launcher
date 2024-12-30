package ru.ricardocraft.backend.service.mirror.modapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class CurseforgeAPI {

    private static final String BASE_URL = "https://api.curseforge.com";

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public CurseforgeAPI(LaunchServerProperties config, ObjectMapper mapper, RestClient restClient) {
        this.apiKey = config.getMirror().getCurseForgeApiKey();
        this.objectMapper = mapper;
        this.restClient = restClient;
    }

    public Mod fetchModById(long id) throws URISyntaxException {
        return restClient.get()
                .uri(new URI(BASE_URL + "/v1/mods/" + id))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
                        throw new IOException("statusCode " + response.getStatusCode());
                    } else {
                        JsonNode element = objectMapper.readTree(response.getBody());
                        return objectMapper.readValue(element.get("data").asText(), Mod.class);
                    }
                });
    }

    public String fetchModDescriptionById(long id) throws URISyntaxException {
        return restClient.get()
                .uri(new URI(BASE_URL + "/v1/mods/" + id + "/description"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
                        throw new IOException("statusCode " + response.getStatusCode());
                    } else {
                        JsonNode element = objectMapper.readTree(response.getBody());
                        return objectMapper.readValue(element.get("data").asText(), String.class);
                    }
                });
    }

    public Artifact fetchModFileById(long modId, long fileId) throws URISyntaxException {
        return restClient.get()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
                        throw new IOException("statusCode " + response.getStatusCode());
                    } else {
                        JsonNode element = objectMapper.readTree(response.getBody());
                        return objectMapper.readValue(element.get("data").asText(), Artifact.class);
                    }
                });
    }

    public String fetchModFileUrlById(long modId, long fileId) throws URISyntaxException {
        return restClient.get()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId + "/download-url"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() < 200 || response.getStatusCode().value() >= 300) {
                        throw new IOException("statusCode " + response.getStatusCode());
                    } else {
                        JsonNode element = objectMapper.readTree(response.getBody());
                        return objectMapper.readValue(element.get("data").asText(), String.class);
                    }
                });
    }


    public record SortableGameVersion(String gameVersionName, String gameVersionPadded, String gameVersion,
                                      String gameVersionReleaseDate, int gameVersionTypeId) {

    }

    public record ModDependency(long modId, int relationType) {

    }

    public record Artifact(long id, long gameId, long modId, String displayName, String fileName, int releaseType,
                           String downloadUrl, List<String> gameVersions,
                           List<SortableGameVersion> sortableGameVersions,
                           List<ModDependency> dependencies, long alternateFileId, boolean isServerPack,
                           long fileFingerprint) {

    }

    public record ArtifactIndex(String gameVersion, long fileId, String filename, int releaseType,
                                int gameVersionTypeId, Integer modLoader) {

    }

    public record Mod(long id, long gameId, String name,
                      long mainFileId, List<Artifact> latestFiles, List<ArtifactIndex> latestFilesIndexes) {
        public long findFileIdByGameVersion(String version) {
            for (var e : latestFilesIndexes) {
                if (e.gameVersion.equals(version)) {
                    return e.fileId;
                }
            }
            throw new RuntimeException("Mod '%s' not supported game version '%s'".formatted(name, version));
        }
    }
}
