package ru.ricardocraft.backend.service.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeDupLibrariesService {

    private final DirectoriesService directoriesService;

    public void deDupLibraries(String clientDir, Boolean isIgnoreLwjgl) throws Exception {
        Path dir = directoriesService.getUpdatesDir().resolve(clientDir).resolve("libraries");
        if (!Files.isDirectory(dir)) {
            throw new FileNotFoundException(dir.toString());
        }
        Map<String, List<Path>> map = new HashMap<>(16);
        IOHelper.walk(dir, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) {
                if (Files.isDirectory(path) && Character.isDigit(path.getFileName().toString().charAt(0))) {
                    String basePath = path.getParent().toString();
                    List<Path> value = map.computeIfAbsent(basePath, k -> new ArrayList<>(1));
                    value.add(path);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path path, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        }, false);
        log.info("Found {} libraries", map.size());
        for (Map.Entry<String, List<Path>> entry : map.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value.size() > 1) {
                if (isIgnoreLwjgl && key.contains("lwjgl")) {
                    log.trace("Path {} skipped (lwjgl found)", key);
                    continue;
                }
                log.info("In path {} found {} libraries", key, value.size());
                var version = value.stream()
                        .filter((f) -> !key.contains("jopt-simple") || !f.getFileName().toString().contains("6.0"))
                        .map(this::convertStringToVersion)
                        .max(Comparator.naturalOrder()).orElse(null);
                log.info("In path {} variants [{}] selected {} version", key,
                        value.stream().map(e -> e.getFileName().toString()).collect(Collectors.joining(", ")),
                        version.originalPath.getFileName().toString());
                Path selectedPath = version.originalPath;
                for (Path path : value) {
                    if (path.equals(selectedPath)) {
                        continue;
                    }
                    log.trace("Delete dir {}", path.toString());
                    IOHelper.deleteDir(path, true);
                }
            }
        }
    }

    private InternalLibraryVersion convertStringToVersion(Path path) {
        String string = path.getFileName().toString();
        string = string.replaceAll("[^.0-9]", "."); // Replace any non-digit character to .
        String[] list = string.split("\\.");
        return new InternalLibraryVersion(Arrays.stream(list)
                .filter(e -> !e.isEmpty()) // Filter ".."
                .mapToLong(Long::parseLong).toArray(), path);
    }

    private static class InternalLibraryVersion implements Comparable<InternalLibraryVersion> {
        private final long[] data;
        private final Path originalPath;

        public InternalLibraryVersion(long[] data, Path originalPath) {
            this.data = data;
            this.originalPath = originalPath;
            //log.debug("LibraryVersion parsed: [{}]", Arrays.stream(data).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
        }

        @Override
        public int compareTo(InternalLibraryVersion some) {
            int result = 0;
            for (int i = 0; i < data.length; ++i) {
                if (i >= some.data.length) break;
                result = Long.compare(data[i], some.data[i]);
                if (result != 0) return result;
            }
            return result;
        }
    }
}
