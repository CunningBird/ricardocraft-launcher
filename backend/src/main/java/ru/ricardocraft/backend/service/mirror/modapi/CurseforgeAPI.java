package ru.ricardocraft.backend.service.mirror.modapi;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.client.HttpRequester;
import ru.ricardocraft.backend.client.error.CurseForgeErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.List;

@Component
public class CurseforgeAPI {

    private static final String BASE_URL = "https://api.curseforge.com";
    private final String apiKey;
    private final HttpRequester requester;

    public CurseforgeAPI(LaunchServerProperties config, HttpRequester requester) {
        this.apiKey = config.getMirror().getCurseForgeApiKey();
        this.requester = requester;
    }

    public Mod fetchModById(long id) throws IOException, URISyntaxException {
        return requester.send(HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + id))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new CurseForgeErrorHandler<>(Mod.class)).getOrThrow();
    }

    public String fetchModDescriptionById(long id) throws IOException, URISyntaxException {
        return requester.send(HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + id + "/description"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new CurseForgeErrorHandler<>(String.class)).getOrThrow();
    }

    public Artifact fetchModFileById(long modId, long fileId) throws IOException, URISyntaxException {
        return requester.send(HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new CurseForgeErrorHandler<>(Artifact.class)).getOrThrow();
    }

    public String fetchModFileUrlById(long modId, long fileId) throws IOException, URISyntaxException {
        return requester.send(HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId + "/download-url"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new CurseForgeErrorHandler<>(String.class)).getOrThrow();
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
