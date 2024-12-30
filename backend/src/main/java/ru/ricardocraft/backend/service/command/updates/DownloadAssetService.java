package ru.ricardocraft.backend.service.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import ru.ricardocraft.backend.base.SizedFile;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.dto.updates.MinecraftVersions;
import ru.ricardocraft.backend.dto.updates.MiniVersion;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.MirrorService;
import ru.ricardocraft.backend.service.UpdatesService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public final class DownloadAssetService {

    private static final String MINECRAFT_VERSIONS_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String RESOURCES_DOWNLOAD_URL = "https://resources.download.minecraft.net/";

    private final RestTemplate restTemplate;
    private final DirectoriesService directoriesService;
    private final MirrorService mirrorService;
    private final UpdatesService updatesService;
    private final ObjectMapper objectMapper;

    public DownloadAssetService(@Qualifier("assetsRestTemplate") RestTemplate restTemplate,
                                DirectoriesService directoriesService,
                                MirrorService mirrorService,
                                UpdatesService updatesService,
                                ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.directoriesService = directoriesService;
        this.mirrorService = mirrorService;
        this.updatesService = updatesService;
        this.objectMapper = objectMapper;
    }

    public void downloadAsset(String versionName, @Nullable String dir, @Nullable String mirrorType) throws Exception {
        String dirName = IOHelper.verifyFileName(dir != null ? dir : "assets");
        String type = mirrorType != null ? mirrorType : "mojang";

        Path assetDir = directoriesService.getUpdatesAssetsDir();
        if (type.equals("mojang")) {
            log.info("Fetch versions from {}", MINECRAFT_VERSIONS_URL);

            var versions = Optional.ofNullable(restTemplate.getForEntity(MINECRAFT_VERSIONS_URL, MinecraftVersions.class).getBody()).orElseThrow();

            String profileUrl = null;
            for (var e : versions.getVersions()) {
                if (e.getId().equals(versionName)) {
                    profileUrl = e.getUrl();
                    break;
                }
            }
            if (profileUrl == null) {
                log.error("Version {} not found", versionName);
                return;
            }
            log.info("Fetch profile {} from {}", versionName, profileUrl);

            var profileInfo = Optional.ofNullable(restTemplate.getForEntity(profileUrl, MiniVersion.class).getBody()).orElseThrow();

            String assetsIndexUrl = profileInfo.getAssetIndex().getUrl();
            String assetIndex = profileInfo.getAssetIndex().getId();
            Path indexPath = assetDir.resolve("indexes").resolve(assetIndex + ".json");
            log.info("Fetch asset index {} from {}", assetIndex, assetsIndexUrl);

            String assetsSerialized = Optional.ofNullable(restTemplate.getForEntity(assetsIndexUrl, String.class).getBody()).orElseThrow();
            JsonNode assets = objectMapper.readTree(assetsSerialized);

            try (Writer writer = IOHelper.newWriter(indexPath)) {
                log.info("Save {}", indexPath);
                writer.write(objectMapper.writeValueAsString(assets));
            }
            if (!assetIndex.equals(versionName)) {
                Path targetPath = assetDir.resolve("indexes").resolve(versionName + ".json");
                log.info("Copy {} into {}", indexPath, targetPath);
                Files.copy(indexPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            List<SizedFile> toDownload = new ArrayList<>(128);

            JsonNode objects = assets.get("objects");
            for (JsonNode e : objects) {
                var hash = e.get("hash").textValue();
                hash = hash.substring(0, 2) + "/" + hash;
                var size = e.get("size").asLong();
                var path = "objects/" + hash;
                var target = assetDir.resolve(path);
                if (Files.exists(target)) {
                    long fileSize = Files.size(target);
                    if (fileSize != size) {
                        log.warn("File {} corrupted. Size {}, expected {}", target, size, fileSize);
                    } else {
                        continue;
                    }
                }
                toDownload.add(new SizedFile(hash, path, size));
            }
            log.info("Download {} files", toDownload.size());
            downloadWithProgressBar(dirName, toDownload, RESOURCES_DOWNLOAD_URL, assetDir);
        } else {
            // Download required asset
            log.info("Downloading asset, it may take some time");
            mirrorService.downloadZip(assetDir, "assets/%s.zip", versionName);
        }

        // Finished
        updatesService.syncUpdatesDir(Collections.singleton(dirName));
        log.info("Asset successfully downloaded: '{}'", dirName);
    }

    private void downloadWithProgressBar(String taskName, List<SizedFile> list, String baseUrl, Path targetDir) throws Exception {
        long total = 0;
        for (SizedFile file : list) {
            if (file.size < 0) {
                continue;
            }
            total += file.size;
        }
        long totalFiles = list.size();
        AtomicLong currentFiles = new AtomicLong(0);
        ProgressBar bar = (new ProgressBarBuilder()).setTaskName(taskName)
                .setInitialMax(total)
                .showSpeed()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setUnit("MB", 1024 * 1024)
                .build();
        bar.setExtraMessage(" [0/%d]".formatted(totalFiles));

        // TODO multi-thread download
        Collections.shuffle(list);
        URI baseUri = baseUrl == null ? null : new URI(baseUrl);
        for (SizedFile file : list) {
            Path filePath = targetDir.resolve(file.filePath);
            IOHelper.createParentDirs(filePath);
            try {
                File ret = new File(String.valueOf(filePath));
                ret.createNewFile();
                File downloaded = restTemplate.execute(createFileUri(baseUri, file.urlPath), HttpMethod.GET, null, clientHttpResponse -> {
                    FileOutputStream fileIO = new FileOutputStream(ret);
                    StreamUtils.copy(clientHttpResponse.getBody(), fileIO);
                    fileIO.close();
                    return ret;
                });
                assert downloaded != null;
                bar.stepBy(downloaded.length());
                bar.setExtraMessage(" [%d/%d]".formatted(currentFiles.incrementAndGet(), totalFiles));
            } catch (Exception exception) {
                log.error("Failed to download {}: Cause {}", file.urlPath != null ? file.urlPath : file.filePath, exception.getMessage());
            }
        }
    }

    private URI createFileUri(URI baseUri, String filePath) throws URISyntaxException {
        if (baseUri != null) {
            String scheme = baseUri.getScheme();
            String host = baseUri.getHost();
            int port = baseUri.getPort();
            if (port != -1)
                host = host + ":" + port;
            String path = baseUri.getPath();
            return new URI(scheme, host, path + filePath, "", "");
        } else {
            return new URI(filePath);
        }
    }
}
