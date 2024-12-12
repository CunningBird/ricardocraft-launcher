package ru.ricardocraft.client.scenes.update;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import ru.ricardocraft.client.base.Downloader;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalActionFile;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.update.UpdateRequest;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.core.hasher.FileNameMatcher;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedFile;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.utils.AssetIndexHelper;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class VisualDownloader {

    private final GuiModuleConfig guiModuleConfig;
    private final LaunchService launchService;

    private final AtomicLong totalDownloaded = new AtomicLong(0);
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private final AtomicLong lastDownloaded = new AtomicLong(0);

    private final AtomicLong totalSize = new AtomicLong();
    private volatile Downloader downloader;

    private final ProgressBar progressBar;
    private final Label speed;
    private final Label volume;

    private final Consumer<Throwable> errorHandle;
    private final Consumer<String> addLog;
    private final Consumer<UpdateScene.DownloadStatus> updateStatus;

    private final ExecutorService executor;

    private final RequestService service;

    public VisualDownloader(RequestService requestService,
                            GuiModuleConfig guiModuleConfig,
                            LaunchService launchService,
                            ProgressBar progressBar,
                            Label speed,
                            Label volume,
                            Consumer<Throwable> errorHandle,
                            Consumer<String> addLog,
                            Consumer<UpdateScene.DownloadStatus> updateStatus) {
        this.guiModuleConfig = guiModuleConfig;
        this.launchService = launchService;

        this.progressBar = progressBar;
        this.speed = speed;
        this.volume = volume;
        this.errorHandle = errorHandle;
        this.addLog = addLog;
        this.executor = new ForkJoinPool(guiModuleConfig.downloadThreads, (pool) -> {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            return thread;
        }, null, true);
        this.updateStatus = updateStatus;
        this.service = requestService;
    }

    public void sendUpdateAssetRequest(String dirName,
                                       Path dir,
                                       FileNameMatcher matcher,
                                       boolean digest,
                                       String assetIndex,
                                       boolean test,
                                       Consumer<HashedDir> onSuccess) {
        if (test) {
            addLog.accept("Hashing %s".formatted(dirName));
            updateStatus.accept(UpdateScene.DownloadStatus.HASHING);
            launchService.workers.submit(() -> {
                try {
                    HashedDir hashedDir = new HashedDir(dir, matcher, false /* TODO */, digest);
                    updateStatus.accept(UpdateScene.DownloadStatus.COMPLETE);
                    onSuccess.accept(hashedDir);
                } catch (IOException e) {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    errorHandle.accept(e);
                }
            });
            return;
        }
        UpdateRequest request = new UpdateRequest(dirName);
        try {
            updateStatus.accept(UpdateScene.DownloadStatus.REQUEST);
            service.request(request).thenAccept(event -> {
                LogHelper.dev("Start updating %s", dirName);
                try {
                    downloadAsset(dirName, dir, matcher, digest, assetIndex, onSuccess, event.hdir, event.url);
                } catch (Exception e) {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(e));
                }
            }).exceptionally((error) -> {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(error.getCause()));
                // hide(2500, scene, onError);
                return null;
            });
        } catch (IOException e) {
            updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
            errorHandle.accept(e);
        }
    }

    public void sendUpdateRequest(String dirName, Path dir, FileNameMatcher matcher, boolean digest, OptionalView view,
                                  boolean optionalsEnabled, boolean test, Consumer<HashedDir> onSuccess) {
        if (test) {
            addLog.accept("Hashing %s".formatted(dirName));
            updateStatus.accept(UpdateScene.DownloadStatus.HASHING);
            launchService.workers.submit(() -> {
                try {
                    HashedDir hashedDir = new HashedDir(dir, matcher, false /* TODO */, digest);
                    updateStatus.accept(UpdateScene.DownloadStatus.COMPLETE);
                    onSuccess.accept(hashedDir);
                } catch (IOException e) {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    errorHandle.accept(e);
                }
            });
            return;
        }
        UpdateRequest request = new UpdateRequest(dirName);
        try {
            updateStatus.accept(UpdateScene.DownloadStatus.REQUEST);
            service.request(request).thenAccept(event -> {
                LogHelper.dev("Start updating %s", dirName);
                try {
                    download(dirName, dir, matcher, digest, view, optionalsEnabled, onSuccess, event.hdir, event.url);
                } catch (Exception e) {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(e));
                }
            }).exceptionally((error) -> {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(error.getCause()));
                // hide(2500, scene, onError);
                return null;
            });
        } catch (IOException e) {
            updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
            errorHandle.accept(e);
        }
    }

    private void download(String dirName,
                          Path dir,
                          FileNameMatcher matcher,
                          boolean digest,
                          OptionalView view,
                          boolean optionalsEnabled,
                          Consumer<HashedDir> onSuccess,
                          HashedDir targetHDir,
                          String baseUrl) throws Exception {
        LinkedList<PathRemapperData> pathRemapper = optionalsEnabled
                ? getPathRemapper(view, targetHDir)
                : new LinkedList<>();
        addLog.accept("Hashing %s".formatted(dirName));
        updateStatus.accept(UpdateScene.DownloadStatus.HASHING);
        if (!IOHelper.exists(dir)) Files.createDirectories(dir);

        HashedDir hashedDir = new HashedDir(dir, matcher, false /* TODO */, digest);
        HashedDir.Diff diff = targetHDir.diff(hashedDir, matcher);
        final List<Downloader.SizedFile> adds = getFilesList(dir, pathRemapper, diff.mismatch);

        LogHelper.info("Diff %d %d", diff.mismatch.size(), diff.extra.size());
        addLog.accept("Downloading %s...".formatted(dirName));
        downloadFiles(dir, adds, baseUrl, () -> {
            try {
                addLog.accept("Delete Extra files %s".formatted(dirName));
                updateStatus.accept(UpdateScene.DownloadStatus.DELETE);
                deleteExtraDir(dir, diff.extra, diff.extra.flag);
                onSuccess.accept(targetHDir);
            } catch (IOException e) {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                errorHandle.accept(e);
            }
        });
    }

    private void downloadAsset(String dirName,
                               Path dir,
                               FileNameMatcher matcher,
                               boolean digest,
                               String assetIndex,
                               Consumer<HashedDir> onSuccess,
                               HashedDir targetHDir,
                               String baseUrl) throws Exception {
        LinkedList<PathRemapperData> pathRemapper = new LinkedList<>();
        addLog.accept("Check assetIndex %s".formatted(assetIndex));
        if (!IOHelper.exists(dir)) Files.createDirectories(dir);
        Consumer<HashedDir> downloadAssetRunnable = (assetHDir) -> {
            try {
                HashedDir hashedDir = new HashedDir(dir, matcher, false, digest);
                HashedDir.Diff diff = assetHDir.diff(hashedDir, matcher);
                final List<Downloader.SizedFile> adds = getFilesList(dir, pathRemapper, diff.mismatch);

                LogHelper.info("Diff %d %d", diff.mismatch.size(), diff.extra.size());
                addLog.accept("Downloading %s...".formatted(dirName));
                downloadFiles(dir, adds, baseUrl, () -> {
                    try {
                        updateStatus.accept(UpdateScene.DownloadStatus.COMPLETE);
                        onSuccess.accept(assetHDir);
                    } catch (Exception e) {
                        updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                        errorHandle.accept(e);
                    }
                });
            } catch (Throwable e) {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                errorHandle.accept(e);
            }
        };

        String assetIndexPath = "indexes/".concat(assetIndex).concat(".json");
        Path localAssetIndexPath = dir.resolve(assetIndexPath);
        boolean needUpdateIndex;
        HashedDir.FindRecursiveResult result = targetHDir.findRecursive(String.valueOf(localAssetIndexPath));
        if (!(result.entry instanceof HashedFile)) {
            addLog.accept("ERROR: assetIndex %s not found".formatted(assetIndex));
            updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
            errorHandle.accept(new RuntimeException("assetIndex not found"));
            return;
        }
        if (Files.exists(localAssetIndexPath)) {
            HashedFile file = new HashedFile(localAssetIndexPath, Files.size(localAssetIndexPath), true);
            needUpdateIndex = ((HashedFile) result.entry).isNotSame(file);
        } else {
            IOHelper.createParentDirs(localAssetIndexPath);
            needUpdateIndex = true;
        }
        if (needUpdateIndex) {
            List<Downloader.SizedFile> adds = new ArrayList<>(1);
            adds.add(new Downloader.SizedFile(assetIndexPath, ((HashedFile) result.entry).size));
            downloadFiles(dir, adds, baseUrl, () -> {
                try {
                    AssetIndexHelper.AssetIndex index = AssetIndexHelper.parse(localAssetIndexPath);
                    AssetIndexHelper.modifyHashedDir(index, targetHDir);
                    downloadAssetRunnable.accept(targetHDir);
                } catch (Exception e) {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    errorHandle.accept(e);
                }
            });
        } else {
            try {
                AssetIndexHelper.AssetIndex index = AssetIndexHelper.parse(localAssetIndexPath);
                AssetIndexHelper.modifyHashedDir(index, targetHDir);
                downloadAssetRunnable.accept(targetHDir);
            } catch (Exception e) {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                errorHandle.accept(e);
            }
        }
    }

    private void downloadFiles(Path dir, List<Downloader.SizedFile> adds, String baseUrl,
                               Runnable onSuccess) {
        ContextHelper.runInFxThreadStatic(this::resetProgress).thenAccept((x) -> {
            try {
                downloader = Downloader.downloadList(adds, baseUrl, dir, new Downloader.DownloadCallback() {
                    @Override
                    public void apply(long fullDiff) {
                        {
                            long old = totalDownloaded.getAndAdd(fullDiff);
                            updateProgress(old, old + fullDiff);
                        }
                    }

                    @Override
                    public void onComplete(Path path) {

                    }
                }, executor, guiModuleConfig.downloadThreads);
                updateStatus.accept(UpdateScene.DownloadStatus.DOWNLOAD);
                downloader.getFuture().thenAccept((e) -> onSuccess.run()).exceptionally((e) -> {
                    updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                    ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(e));
                    return null;
                });
            } catch (Throwable e) {
                updateStatus.accept(UpdateScene.DownloadStatus.ERROR);
                ContextHelper.runInFxThreadStatic(() -> errorHandle.accept(e));
            }
        });
    }

    private void resetProgress() {
        totalDownloaded.set(0);
        lastUpdateTime.set(System.currentTimeMillis());
        lastDownloaded.set(0);
        progressBar.progressProperty().setValue(0);
    }

    private List<Downloader.SizedFile> getFilesList(Path dir, LinkedList<PathRemapperData> pathRemapper,
                                                    HashedDir mismatch) throws IOException {
        totalSize.set(0);

        final List<Downloader.SizedFile> adds = new ArrayList<>();
        mismatch.walk(IOHelper.CROSS_SEPARATOR, (path, name, entry) -> {
            String urlPath = path;
            switch (entry.getType()) {
                case FILE -> {
                    HashedFile file = (HashedFile) entry;
                    totalSize.addAndGet(file.size);
                    for (PathRemapperData remapEntry : pathRemapper) {
                        if (path.startsWith(remapEntry.key)) {
                            urlPath = path.replace(remapEntry.key, remapEntry.value);
                            LogHelper.dev("Remap found: injected url path: %s | calculated original url path: %s", path,
                                    urlPath);
                        }
                    }
                    Files.deleteIfExists(dir.resolve(path));
                    adds.add(new Downloader.SizedFile(urlPath, path, file.size));
                }
                case DIR -> Files.createDirectories(dir.resolve(path));
            }
            return HashedDir.WalkAction.CONTINUE;
        });
        return adds;
    }

    private LinkedList<PathRemapperData> getPathRemapper(OptionalView view, HashedDir hdir) {
        for (OptionalAction action : view.getDisabledActions()) {
            if (action instanceof OptionalActionFile optionalActionFile) {
                optionalActionFile.disableInHashedDir(hdir);
            }
        }
        LinkedList<PathRemapperData> pathRemapper = new LinkedList<>();
        Set<OptionalActionFile> fileActions = view.getActionsByClass(OptionalActionFile.class);
        for (OptionalActionFile file : fileActions) {
            file.injectToHashedDir(hdir);
            file.files.forEach((k, v) -> {
                if (v == null || v.isEmpty()) return;
                pathRemapper.add(new PathRemapperData(v, k)); //reverse (!)
                LogHelper.dev("Remap prepare %s to %s", v, k);
            });
        }
        pathRemapper.sort(Comparator.comparingInt(c -> -c.key.length())); // Support deep remap
        return pathRemapper;
    }

    private void deleteExtraDir(Path subDir, HashedDir subHDir, boolean deleteDir) throws IOException {
        for (Map.Entry<String, HashedEntry> mapEntry : subHDir.map().entrySet()) {
            String name = mapEntry.getKey();
            Path path = subDir.resolve(name);

            // Delete list and dirs based on type
            HashedEntry entry = mapEntry.getValue();
            HashedEntry.Type entryType = entry.getType();
            switch (entryType) {
                case FILE -> Files.delete(path);
                case DIR -> deleteExtraDir(path, (HashedDir) entry, deleteDir || entry.flag);
                default -> throw new AssertionError("Unsupported hashed entry type: " + entryType.name());
            }
        }

        // Delete!
        if (deleteDir) {
            Files.delete(subDir);
        }
    }

    private static class PathRemapperData {
        public String key;
        public String value;

        public PathRemapperData(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }


    private void updateProgress(long oldValue, long newValue) {
        double add = (double) (newValue - oldValue) / (double) totalSize.get(); // 0.0 - 1.0
        DoubleProperty property = progressBar.progressProperty();
        property.set(property.get() + add);
        long lastTime = lastUpdateTime.get();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 130) {
            double bytesSpeed = (double) (newValue - lastDownloaded.get()) / (double) (currentTime - lastTime) * 1000.0;
            String speedFormat = "%.1f".formatted(bytesSpeed * 8 / (1000.0 * 1000.0));
            ContextHelper.runInFxThreadStatic(() -> {
                volume.setText("%.1f/%.1f MB".formatted((double) newValue / (1024.0 * 1024.0),
                        (double) totalSize.get() / (1024.0 * 1024.0)));
                speed.setText(speedFormat);
            });
            lastUpdateTime.set(currentTime);
            lastDownloaded.set(newValue);
        }
    }

    public boolean isDownload() {
        return downloader != null && !downloader.getFuture().isDone();
    }

    public void cancel() {
        downloader.cancel();
    }

}
