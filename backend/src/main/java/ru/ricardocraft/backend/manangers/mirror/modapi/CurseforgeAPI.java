package ru.ricardocraft.backend.manangers.mirror.modapi;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.HttpSender;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

@Component
public class CurseforgeAPI {
    private static final String BASE_URL = "https://api.curseforge.com";
    private final String apiKey;
    private final HttpSender sender;
    private final HttpClient client = HttpClient.newBuilder().build();

    public CurseforgeAPI(LaunchServerConfig config, HttpSender sender) {
        this.apiKey = config.mirrorConfig.curseforgeApiKey;
        this.sender = sender;
    }

    public Mod fetchModById(long id) throws IOException, URISyntaxException {
        return sender.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + id))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new HttpSender.CurseForgeErrorHandler<>(Mod.class)).getOrThrow();
    }

    public String fetchModDescriptionById(long id) throws IOException, URISyntaxException {
        return sender.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + id + "/description"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new HttpSender.CurseForgeErrorHandler<>(String.class)).getOrThrow();
    }

    public Artifact fetchModFileById(long modId, long fileId) throws IOException, URISyntaxException {
        return sender.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new HttpSender.CurseForgeErrorHandler<>(Artifact.class)).getOrThrow();
    }

    public String fetchModFileUrlById(long modId, long fileId) throws IOException, URISyntaxException {
        return sender.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(new URI(BASE_URL + "/v1/mods/" + modId + "/files/" + fileId + "/download-url"))
                .header("Accept", "application/json")
                .header("x-api-key", apiKey)
                .build(), new HttpSender.CurseForgeErrorHandler<>(String.class)).getOrThrow();
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
