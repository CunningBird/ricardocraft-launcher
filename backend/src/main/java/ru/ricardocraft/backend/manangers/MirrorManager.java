package ru.ricardocraft.backend.manangers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.manangers.mirror.WorkspaceTools;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class MirrorManager {

    private transient final Logger logger = LogManager.getLogger(MirrorManager.class);

    protected final ArrayList<Mirror> list = new ArrayList<>();
    private transient final HttpClient client = HttpClient.newBuilder().build();
    @Getter
    private Mirror defaultMirror;
    @Getter
    private final WorkspaceTools tools;

    private transient final JacksonManager jacksonManager;

    @Autowired
    public MirrorManager(LaunchServerConfig config, WorkspaceTools tools, JacksonManager jacksonManager) {
        this.tools = tools;
        this.jacksonManager = jacksonManager;

        Arrays.stream(config.mirrors).forEach(this::addMirror);
    }

    public void addMirror(String mirror) {
        Mirror m = new Mirror(mirror);
        m.enabled = true;
        if (defaultMirror == null) defaultMirror = m;
        list.add(m);
    }

    public int size() {
        return list.size();
    }

    public boolean downloadZip(Mirror mirror, Path path, String mask, Object... args) throws IOException {
        if (!mirror.enabled) return false;
        URL url = mirror.getURL(mask, args);
        logger.debug("Try download {}", url.toString());
        try {
            downloadZip(url, path);
        } catch (IOException e) {
            logger.error("Download {} failed({}: {})", url.toString(), e.getClass().getName(), e.getMessage());
            return false;
        }
        return true;
    }

    public void downloadZip(Path path, String mask, Object... args) throws IOException {
        if (downloadZip(defaultMirror, path, mask, args)) {
            return;
        }
        for (Mirror mirror : list) {
            if (mirror != defaultMirror) {
                if (downloadZip(mirror, path, mask, args)) return;
            }
        }
        throw new IOException("Error download %s. All mirrors return error".formatted(path.toString()));
    }

    public JsonNode jsonRequest(Mirror mirror, JsonNode request, String method, String mask, Object... args) throws IOException {
        if (!mirror.enabled) return null;
        URL url = mirror.getURL(mask, args);
        try {
            var response = client.send(HttpRequest.newBuilder()
                            .method(method, request == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(jacksonManager.getMapper().writeValueAsString(request)))
                            .uri(url.toURI())
                    .build(), HttpResponse.BodyHandlers.ofString());
            return jacksonManager.getMapper().readTree(response.body());
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.error("JsonRequest {} failed({}: {})", url.toString(), e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    public JsonNode jsonRequest(JsonNode request, String method, String mask, Object... args) throws IOException {
        JsonNode result = jsonRequest(defaultMirror, request, method, mask, args);
        if (result != null) return result;
        for (Mirror mirror : list) {
            if (mirror != defaultMirror) {
                result = jsonRequest(mirror, request, method, mask, args);
                if (result != null) return result;
            }
        }
        throw new IOException("Error jsonRequest. All mirrors return error");
    }

    private void downloadZip(URL url, Path dir) throws IOException {
        try (ZipInputStream input = IOHelper.newZipInput(url)) {
            Files.createDirectory(dir);
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (entry.isDirectory()) {
                    Files.createDirectory(dir.resolve(IOHelper.toPath(entry.getName())));
                    continue;
                }
                // Unpack entry
                String name = entry.getName();
                logger.debug("Downloading file: '{}'", name);
                IOHelper.transfer(input, dir.resolve(IOHelper.toPath(name)));
            }
        }
    }

    public static class Mirror {
        final String baseUrl;
        boolean enabled;

        Mirror(String url) {
            baseUrl = url;
        }

        private URL formatArgs(String mask, Object... args) throws MalformedURLException {
            Object[] data = Arrays.stream(args).map(e -> IOHelper.urlEncode(e.toString())).toArray();
            return new URL(baseUrl.concat(mask.formatted(data)));
        }

        public URL getURL(String mask, Object... args) throws MalformedURLException {
            return formatArgs(mask, args);
        }
    }
}
