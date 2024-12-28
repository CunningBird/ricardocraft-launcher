package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper.DigestAlgorithm;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public final class IndexAssetCommand {

    private static final String INDEXES_DIR = "indexes";
    private static final String OBJECTS_DIR = "objects";
    private static final String JSON_EXTENSION = ".json";

    private transient final DirectoriesManager directoriesManager;
    private transient final UpdatesManager updatesManager;
    private transient final ObjectMapper objectMapper;

    public static Path resolveIndexFile(Path assetDir, String name) {
        return assetDir.resolve(INDEXES_DIR).resolve(name + JSON_EXTENSION);
    }

    public static Path resolveObjectFile(Path assetDir, String hash) {
        return assetDir.resolve(OBJECTS_DIR).resolve(hash.substring(0, 2)).resolve(hash);
    }

    @ShellMethod("[dir] [index] [output-dir] Index asset dir (1.7.10+)")
    public void indexAsset(@ShellOption String indexInputAssetDirName,
                           @ShellOption String indexIndexFileName,
                           @ShellOption String outputOutputAssetDirName) throws Exception {
        String inputAssetDirName = IOHelper.verifyFileName(indexInputAssetDirName);
        String indexFileName = IOHelper.verifyFileName(indexIndexFileName);
        String outputAssetDirName = IOHelper.verifyFileName(outputOutputAssetDirName);
        Path inputAssetDir = directoriesManager.getUpdatesDir().resolve(inputAssetDirName);
        Path outputAssetDir = directoriesManager.getUpdatesDir().resolve(outputAssetDirName);
        if (outputAssetDir.equals(inputAssetDir))
            throw new Exception("Unindexed and indexed asset dirs can't be same");

        // Create new asset dir
        log.info("Creating indexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Index objects
        Map<String, JsonNode> objects = Map.of();
        log.info("Indexing objects");
        IOHelper.walk(inputAssetDir, new IndexAssetVisitor(objects, inputAssetDir, outputAssetDir), false);

        // Write index file
        log.info("Writing asset index file: '{}'", indexFileName);

        try (BufferedWriter writer = IOHelper.newWriter(resolveIndexFile(outputAssetDir, indexFileName))) {
            writer.write(objectMapper.writeValueAsString(objects));
        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        log.info("Asset successfully indexed: '{}'", inputAssetDirName);
    }

    public static class IndexObject {
        final long size;
        final String hash;

        public IndexObject(long size, String hash) {
            this.size = size;
            this.hash = hash;
        }
    }

    private final class IndexAssetVisitor extends SimpleFileVisitor<Path> {
        private final Map<String, JsonNode> objects;
        private final Path inputAssetDir;
        private final Path outputAssetDir;

        private IndexAssetVisitor(Map<String, JsonNode> objects, Path inputAssetDir, Path outputAssetDir) {
            this.objects = objects;
            this.inputAssetDir = inputAssetDir;
            this.outputAssetDir = outputAssetDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String name = IOHelper.toString(inputAssetDir.relativize(file));
            log.info("Indexing: '{}'", name);

            // Add to index and copy file
            String digest = SecurityHelper.toHex(SecurityHelper.digest(DigestAlgorithm.SHA1, file));
            IndexObject obj = new IndexObject(attrs.size(), digest);
            objects.put(name, objectMapper.valueToTree(obj));
            IOHelper.copy(file, resolveObjectFile(outputAssetDir, digest));

            // Continue visiting
            return super.visitFile(file, attrs);
        }
    }
}
