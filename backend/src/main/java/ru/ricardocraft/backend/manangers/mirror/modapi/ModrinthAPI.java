package ru.ricardocraft.backend.manangers.mirror.modapi;

import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.utils.Version;
import ru.ricardocraft.backend.socket.HttpSender;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;

@Component
public class ModrinthAPI {
    private static final String BASE_URL = "https://api.modrinth.com/v2/";
    private static final String userAgent = "GravitLauncher/%s MirrorHelper/%s".formatted(Version.getVersion().getVersionString(), "1.0.0");

    public final HttpClient client = HttpClient.newBuilder().build();

    public final HttpSender sender;

    @Autowired
    public ModrinthAPI(HttpSender sender) {
        this.sender = sender;
    }

    @SuppressWarnings("unchecked")
    public List<ModVersionData> getMod(String slug) throws IOException {
        TypeToken<List<ModVersionData>> typeToken = new TypeToken<>(){};
        return (List<ModVersionData>) sender.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL.concat("project/%s/version".formatted(slug))))
                .header("User-Agent", userAgent)
                .build(), new HttpSender.ModrinthErrorHandler<>(typeToken.getType())).getOrThrow();
    }

    public ModVersionData getModByGameVersion(List<ModVersionData> list, String gameVersion, String loader) {
        for(var e : list) {
            if(!e.loaders.contains(loader)) {
                continue;
            }
            if(!e.game_versions.contains(gameVersion)) {
                continue;
            }
            return e;
        }
        return null;
    }

    public record ModVersionData(String id, String name, List<ModVersionFileData> files, List<String> game_versions, List<String> loaders) {

    }

    public record ModVersionFileData(Map<String, String> hashes, String url, String filename, boolean primary) {

    }
}
