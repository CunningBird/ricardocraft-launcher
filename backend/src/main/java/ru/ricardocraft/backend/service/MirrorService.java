package ru.ricardocraft.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.mirror.Mirror;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class MirrorService {

    private final HttpClient client = HttpClient.newBuilder().build();

    @Getter
    private final Mirror defaultMirror;
    private final List<Mirror> list;

    private final ObjectMapper objectMapper;

    @Autowired
    public MirrorService(LaunchServerProperties config, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.list = Arrays.stream(config.getMirrors())
                .map(Mirror::new)
                .peek(mirror -> mirror.setEnabled(true))
                .collect(Collectors.toList());

        this.defaultMirror = list.stream()
                .findFirst()
                .orElse(null);
    }

    public boolean downloadZip(Mirror mirror, Path path, String mask, Object... args) throws IOException {
        if (!mirror.getEnabled()) return false;
        URL url = mirror.getURL(mask, args);
        log.debug("Try download {}", url.toString());
        try {
            downloadZip(url, path);
        } catch (IOException e) {
            log.error("Download {} failed({}: {})", url.toString(), e.getClass().getName(), e.getMessage());
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
        if (!mirror.getEnabled()) return null;
        URL url = mirror.getURL(mask, args);
        try {
            var response = client.send(HttpRequest.newBuilder()
                    .method(method, request == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .uri(url.toURI())
                    .build(), HttpResponse.BodyHandlers.ofString());
            return objectMapper.readTree(response.body());
        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.error("JsonRequest {} failed({}: {})", url.toString(), e.getClass().getName(), e.getMessage());
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
                log.debug("Downloading file: '{}'", name);
                IOHelper.transfer(input, dir.resolve(IOHelper.toPath(name)));
            }
        }
    }
}
