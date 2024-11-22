package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Component
public final class DownloadAssetCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(DownloadAssetCommand.class);

    private static final String MINECRAFT_VERSIONS_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String RESOURCES_DOWNLOAD_URL = "https://resources.download.minecraft.net/";

    private transient final DirectoriesManager directoriesManager;
    private transient final MirrorManager mirrorManager;
    private transient final UpdatesManager updatesManager;
    private transient final JacksonManager jacksonManager;
    private transient final HttpRequester requester;

    @Autowired
    public DownloadAssetCommand(DirectoriesManager directoriesManager,
                                MirrorManager mirrorManager,
                                UpdatesManager updatesManager,
                                JacksonManager jacksonManager,
                                HttpRequester requester) {
        super();
        this.directoriesManager = directoriesManager;
        this.mirrorManager = mirrorManager;
        this.updatesManager = updatesManager;
        this.jacksonManager = jacksonManager;
        this.requester = requester;
    }

    @Override
    public String getArgsDescription() {
        return "[version] [dir] (mojang/mirror)";
    }

    @Override
    public String getUsageDescription() {
        return "Download asset dir";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        String versionName = args[0];
        String dirName = IOHelper.verifyFileName(args.length > 1 ? args[1] : "assets");
        String type = args.length > 2 ? args[2] : "mojang";

        Path assetDir = directoriesManager.getUpdatesAssetsDir();
        if (type.equals("mojang")) {
            logger.info("Fetch versions from {}", MINECRAFT_VERSIONS_URL);
            var versions = requester.send(requester.get(MINECRAFT_VERSIONS_URL, null), MinecraftVersions.class).getOrThrow();
            String profileUrl = null;
            for (var e : versions.getVersions()) {
                if (e.getId().equals(versionName)) {
                    profileUrl = e.getUrl();
                    break;
                }
            }
            if (profileUrl == null) {
                logger.error("Version {} not found", versionName);
                return;
            }
            logger.info("Fetch profile {} from {}", versionName, profileUrl);
            var profileInfo = requester.send(requester.get(profileUrl, null), MiniVersion.class).getOrThrow();
            String assetsIndexUrl = profileInfo.getAssetIndex().getUrl();
            String assetIndex = profileInfo.getAssetIndex().getId();
            Path indexPath = assetDir.resolve("indexes").resolve(assetIndex + ".json");
            logger.info("Fetch asset index {} from {}", assetIndex, assetsIndexUrl);
            JsonNode assets = requester.send(requester.get(assetsIndexUrl, null), JsonNode.class).getOrThrow();

            try (Writer writer = IOHelper.newWriter(indexPath)) {
                logger.info("Save {}", indexPath);
                writer.write(jacksonManager.getMapper().writeValueAsString(assets));
            }
            if (!assetIndex.equals(versionName)) {
                Path targetPath = assetDir.resolve("indexes").resolve(versionName + ".json");
                logger.info("Copy {} into {}", indexPath, targetPath);
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
                        logger.warn("File {} corrupted. Size {}, expected {}", target, size, fileSize);
                    } else {
                        continue;
                    }
                }
                toDownload.add(new Downloader.SizedFile(hash, path, size));
            }
            logger.info("Download {} files", toDownload.size());
            Downloader downloader = downloadWithProgressBar(dirName, toDownload, RESOURCES_DOWNLOAD_URL, assetDir);
            downloader.getFuture().get();
        } else {
            // Download required asset
            logger.info("Downloading asset, it may take some time");
            mirrorManager.downloadZip(assetDir, "assets/%s.zip", versionName);
        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(dirName));
        logger.info("Asset successfully downloaded: '{}'", dirName);
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
