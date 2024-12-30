package ru.ricardocraft.backend.service.auth.updates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.hasher.HashedDir;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.hasher.HInput;
import ru.ricardocraft.backend.base.hasher.HOutput;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
public class LocalUpdatesProvider extends UpdatesProvider {

    private final LaunchServerProperties config;
    private final DirectoriesService directoriesService;

    private volatile transient Map<String, HashedDir> updatesDirMap;

    @Autowired
    public LocalUpdatesProvider(LaunchServerProperties config, DirectoriesService directoriesService) {
        this.config = config;
        this.directoriesService = directoriesService;
    }

    @Override
    public void syncInitially() throws IOException {
        readUpdatesDir();
    }

    @Override
    public HashedDir getUpdatesDir(String updateName) {
        return updatesDirMap.get(updateName);
    }

    @Override
    public void upload(String updateName, Map<String, Path> files, boolean deleteAfterUpload) throws IOException {
        var path = resolveUpdateName(updateName);
        for (var e : files.entrySet()) {
            var target = path.resolve(e.getKey());
            var source = e.getValue();
            IOHelper.createParentDirs(target);
            if (deleteAfterUpload) {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Override
    public Map<String, Path> download(String updateName, List<String> files) {
        var path = resolveUpdateName(updateName);
        Map<String, Path> map = new HashMap<>();
        for (var e : files) {
            map.put(e, path.resolve(e));
        }
        return map;
    }

    @Override
    public void delete(String updateName, List<String> files) throws IOException {
        var path = resolveUpdateName(updateName);
        for (var e : files) {
            var target = path.resolve(e);
            Files.delete(target);
        }
    }

    @Override
    public void delete(String updateName) throws IOException {
        var path = resolveUpdateName(updateName);
        IOHelper.deleteDir(path, true);
    }

    @Override
    public void create(String updateName) throws IOException {
        var path = resolveUpdateName(updateName);
        Files.createDirectories(path);
    }

    private void writeCache(Path file) throws IOException {
        try (HOutput output = new HOutput(IOHelper.newOutput(file))) {
            output.writeLength(updatesDirMap.size(), 0);
            for (Map.Entry<String, HashedDir> entry : updatesDirMap.entrySet()) {
                output.writeString(entry.getKey(), 0);
                entry.getValue().write(output);
            }
        }
        log.debug("Saved {} updates to cache", updatesDirMap.size());
    }

    private void readCache(Path file) throws IOException {
        Map<String, HashedDir> updatesDirMap = new HashMap<>(16);
        try (HInput input = new HInput(IOHelper.newInput(file))) {
            int size = input.readLength(0);
            for (int i = 0; i < size; ++i) {
                String name = input.readString(0);
                HashedDir dir = new HashedDir(input);
                updatesDirMap.put(name, dir);
            }
        }
        log.debug("Found {} updates from cache", updatesDirMap.size());
        this.updatesDirMap = Collections.unmodifiableMap(updatesDirMap);
    }

    public void readUpdatesDir() throws IOException {
        var cacheFilePath = directoriesService.getCacheFile();
        if (config.getLocalUpdatesProvider().getCacheUpdates()) {
            if (Files.exists(cacheFilePath)) {
                try {
                    readCache(cacheFilePath);
                    return;
                } catch (Throwable e) {
                    log.error("Read updates cache failed", e);
                }
            }
        }
        sync(null);
    }

    public void sync(Collection<String> dirs) throws IOException {
        log.info("Syncing updates dir");
        Map<String, HashedDir> newUpdatesDirMap = new HashMap<>(16);
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directoriesService.getUpdatesDir())) {
            for (final Path updateDir : dirStream) {
                if (Files.isHidden(updateDir))
                    continue; // Skip hidden

                // Resolve name and verify is dir
                String name = IOHelper.getFileName(updateDir);
                if (!IOHelper.isDir(updateDir)) {
                    if (!IOHelper.isFile(updateDir) && Stream.of(".jar", ".exe", ".hash").noneMatch(e -> updateDir.toString().endsWith(e)))
                        log.warn("Not update dir: '{}'", name);
                    continue;
                }

                // Add from previous map (it's guaranteed to be non-null)
                if (dirs != null && !dirs.contains(name)) {
                    HashedDir hdir = updatesDirMap.get(name);
                    if (hdir != null) {
                        newUpdatesDirMap.put(name, hdir);
                        continue;
                    }
                }

                // Sync and sign update dir
                log.info("Syncing '{}' update dir", name);
                HashedDir updateHDir = new HashedDir(updateDir, null, true, true);
                newUpdatesDirMap.put(name, updateHDir);
            }
        }
        updatesDirMap = Collections.unmodifiableMap(newUpdatesDirMap);
        if (config.getLocalUpdatesProvider().getCacheUpdates()) {
            try {
                writeCache(directoriesService.getCacheFile());
            } catch (Throwable e) {
                log.error("Write updates cache failed", e);
            }
        }
    }

    private Path resolveUpdateName(String updateName) {
        if (updateName == null) return directoriesService.getUpdatesDir();
        return directoriesService.getUpdatesDir().resolve(updateName);
    }
}
