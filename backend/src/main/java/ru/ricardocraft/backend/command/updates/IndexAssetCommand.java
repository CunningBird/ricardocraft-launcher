package ru.ricardocraft.backend.command.updates;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.CommandException;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper.DigestAlgorithm;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

@Component
public final class IndexAssetCommand extends Command {

    private transient final Logger logger = LogManager.getLogger();

    private transient final LaunchServerDirectories directories;
    private transient final UpdatesManager updatesManager;

    public static final String INDEXES_DIR = "indexes";
    public static final String OBJECTS_DIR = "objects";
    private static final String JSON_EXTENSION = ".json";

    @Autowired
    public IndexAssetCommand(LaunchServerDirectories directories, UpdatesManager updatesManager) {
        super();
        this.directories = directories;
        this.updatesManager = updatesManager;
    }

    public static Path resolveIndexFile(Path assetDir, String name) {
        return assetDir.resolve(INDEXES_DIR).resolve(name + JSON_EXTENSION);
    }

    public static Path resolveObjectFile(Path assetDir, String hash) {
        return assetDir.resolve(OBJECTS_DIR).resolve(hash.substring(0, 2)).resolve(hash);
    }

    @Override
    public String getArgsDescription() {
        return "[dir] [index] [output-dir]";
    }

    @Override
    public String getUsageDescription() {
        return "Index asset dir (1.7.10+)";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 3);
        String inputAssetDirName = IOHelper.verifyFileName(args[0]);
        String indexFileName = IOHelper.verifyFileName(args[1]);
        String outputAssetDirName = IOHelper.verifyFileName(args[2]);
        Path inputAssetDir = directories.updatesDir.resolve(inputAssetDirName);
        Path outputAssetDir = directories.updatesDir.resolve(outputAssetDirName);
        if (outputAssetDir.equals(inputAssetDir))
            throw new CommandException("Unindexed and indexed asset dirs can't be same");

        // Create new asset dir
        logger.info("Creating indexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Index objects
        JsonObject objects = new JsonObject();
        logger.info("Indexing objects");
        IOHelper.walk(inputAssetDir, new IndexAssetVisitor(objects, inputAssetDir, outputAssetDir), false);

        // Write index file
        logger.info("Writing asset index file: '{}'", indexFileName);

        try (BufferedWriter writer = IOHelper.newWriter(resolveIndexFile(outputAssetDir, indexFileName))) {
            JsonObject result = new JsonObject();
            result.add("objects", objects);
            writer.write(Launcher.gsonManager.gson.toJson(result));
        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        logger.info("Asset successfully indexed: '{}'", inputAssetDirName);
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
        private final JsonObject objects;
        private final Path inputAssetDir;
        private final Path outputAssetDir;

        private IndexAssetVisitor(JsonObject objects, Path inputAssetDir, Path outputAssetDir) {
            this.objects = objects;
            this.inputAssetDir = inputAssetDir;
            this.outputAssetDir = outputAssetDir;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String name = IOHelper.toString(inputAssetDir.relativize(file));
            logger.info("Indexing: '{}'", name);

            // Add to index and copy file
            String digest = SecurityHelper.toHex(SecurityHelper.digest(DigestAlgorithm.SHA1, file));
            IndexObject obj = new IndexObject(attrs.size(), digest);
            objects.add(name, Launcher.gsonManager.gson.toJsonTree(obj));
            IOHelper.copy(file, resolveObjectFile(outputAssetDir, digest));

            // Continue visiting
            return super.visitFile(file, attrs);
        }
    }
}
