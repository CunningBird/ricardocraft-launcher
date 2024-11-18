package ru.ricardocraft.backend.manangers.mirror.modapi;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.HttpHelper;
import ru.ricardocraft.backend.base.utils.Version;
import ru.ricardocraft.backend.manangers.GsonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class ModrinthAPI {
    private static final String BASE_URL = "https://api.modrinth.com/v2/";
    private static final String userAgent = "GravitLauncher/%s MirrorHelper/%s".formatted(Version.getVersion().getVersionString(), "1.0.0");

    public final HttpClient client = HttpClient.newBuilder().build();

    public final GsonManager gsonManager;

    @Autowired
    public ModrinthAPI(GsonManager gsonManager) {
        this.gsonManager = gsonManager;
    }

    @SuppressWarnings("unchecked")
    public List<ModVersionData> getMod(String slug) throws IOException {
        TypeToken<List<ModVersionData>> typeToken = new TypeToken<>(){};
        return (List<ModVersionData>) HttpHelper.send(client, HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL.concat("project/%s/version".formatted(slug))))
                .header("User-Agent", userAgent)
                .build(), new ModrinthErrorHandler<>(typeToken.getType()), gsonManager).getOrThrow();
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

    public class ModrinthErrorHandler<T> implements HttpHelper.HttpErrorHandler<T, Void> {
        private final Type type;

        public ModrinthErrorHandler(Type type) {
            this.type = type;
        }

        @Override
        public HttpHelper.HttpOptional<T, Void> apply(HttpResponse<InputStream> response, GsonManager gsonManager) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new HttpHelper.HttpOptional<>(null, null, response.statusCode());
            }
            try (Reader reader = new InputStreamReader(response.body())) {
                JsonElement element = gsonManager.gson.fromJson(reader, JsonElement.class);
                return new HttpHelper.HttpOptional<>(gsonManager.gson.fromJson(element, type), null, response.statusCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
