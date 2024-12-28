package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.socket.Downloader;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.dto.updates.MinecraftVersions;
import ru.ricardocraft.backend.dto.updates.MiniVersion;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.socket.HttpRequester;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public final class DownloadAssetCommand {

    private static final String MINECRAFT_VERSIONS_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String RESOURCES_DOWNLOAD_URL = "https://resources.download.minecraft.net/";

    private final DirectoriesManager directoriesManager;
    private final MirrorManager mirrorManager;
    private final UpdatesManager updatesManager;
    private final JacksonManager jacksonManager;
    private final HttpRequester requester;

    @ShellMethod("[version] [dir] (mojang/mirror) Download asset dir")
    public void downloadAsset(@ShellOption String versionName,
                       @ShellOption(defaultValue = ShellOption.NULL) String dir,
                       @ShellOption(defaultValue = ShellOption.NULL) String mirrorType) throws Exception {
        String dirName = IOHelper.verifyFileName(dir != null ? dir : "assets");
        String type = mirrorType != null ? mirrorType : "mojang";

        Path assetDir = directoriesManager.getUpdatesAssetsDir();
        if (type.equals("mojang")) {
            log.info("Fetch versions from {}", MINECRAFT_VERSIONS_URL);
            var versions = requester.send(requester.get(MINECRAFT_VERSIONS_URL, null), MinecraftVersions.class).getOrThrow();
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
            var profileInfo = requester.send(requester.get(profileUrl, null), MiniVersion.class).getOrThrow();
            String assetsIndexUrl = profileInfo.getAssetIndex().getUrl();
            String assetIndex = profileInfo.getAssetIndex().getId();
            Path indexPath = assetDir.resolve("indexes").resolve(assetIndex + ".json");
            log.info("Fetch asset index {} from {}", assetIndex, assetsIndexUrl);
            JsonNode assets = requester.send(requester.get(assetsIndexUrl, null), JsonNode.class).getOrThrow();

            try (Writer writer = IOHelper.newWriter(indexPath)) {
                log.info("Save {}", indexPath);
                writer.write(jacksonManager.getMapper().writeValueAsString(assets));
            }
            if (!assetIndex.equals(versionName)) {
                Path targetPath = assetDir.resolve("indexes").resolve(versionName + ".json");
                log.info("Copy {} into {}", indexPath, targetPath);
                Files.copy(indexPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            List<Downloader.SizedFile> toDownload = new ArrayList<>(128);

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
                toDownload.add(new Downloader.SizedFile(hash, path, size));
            }
            log.info("Download {} files", toDownload.size());
            Downloader downloader = downloadWithProgressBar(dirName, toDownload, RESOURCES_DOWNLOAD_URL, assetDir);
            downloader.getFuture().get();
        } else {
            // Download required asset
            log.info("Downloading asset, it may take some time");
            mirrorManager.downloadZip(assetDir, "assets/%s.zip", versionName);
        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(dirName));
        log.info("Asset successfully downloaded: '{}'", dirName);
    }

    private Downloader downloadWithProgressBar(String taskName, List<Downloader.SizedFile> list, String baseUrl, Path targetDir) throws Exception {
        long total = 0;
        for (Downloader.SizedFile file : list) {
            if (file.size < 0) {
                continue;
            }
            total += file.size;
        }
        long totalFiles = list.size();
        AtomicLong current = new AtomicLong(0);
        AtomicLong currentFiles = new AtomicLong(0);
        ProgressBar bar = (new ProgressBarBuilder()).setTaskName(taskName)
                .setInitialMax(total)
                .showSpeed()
                .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BLOCK)
                .setUnit("MB", 1024 * 1024)
                .build();
        bar.setExtraMessage(" [0/%d]".formatted(totalFiles));
        Downloader downloader = Downloader.downloadList(list, baseUrl, targetDir, new Downloader.DownloadCallback() {
            @Override
            public void apply(long fullDiff) {
                current.addAndGet(fullDiff);
                bar.stepBy(fullDiff);
            }

            @Override
            public void onComplete(Path path) {
                bar.setExtraMessage(" [%d/%d]".formatted(currentFiles.incrementAndGet(), totalFiles));
            }
        }, null, 4);
        downloader.getFuture().handle((v, e) -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            bar.close();
            if (e != null) {
                future.completeExceptionally(e);
            } else {
                future.complete(null);
            }
            return future;
        });
        return downloader;
    }
}
