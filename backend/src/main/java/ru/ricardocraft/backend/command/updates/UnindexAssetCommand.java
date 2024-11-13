package ru.ricardocraft.backend.command.updates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.CommandException;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Component
public final class UnindexAssetCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    private transient final LaunchServerDirectories directories;
    private transient final UpdatesManager updatesManager;

    @Autowired
    public UnindexAssetCommand(LaunchServerDirectories directories, UpdatesManager updatesManager) {
        super();
        this.directories = directories;
        this.updatesManager = updatesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[dir] [index] [output-dir]";
    }

    @Override
    public String getUsageDescription() {
        return "Unindex asset dir (1.7.10+)";
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
            throw new CommandException("Indexed and unindexed asset dirs can't be same");

        // Create new asset dir
        logger.info("Creating unindexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Read JSON file
        JsonObject objects;
        logger.info("Reading asset index file: '{}'", indexFileName);
        try (BufferedReader reader = IOHelper.newReader(IndexAssetCommand.resolveIndexFile(inputAssetDir, indexFileName))) {
            objects = JsonParser.parseReader(reader).getAsJsonObject().get("objects").getAsJsonObject();
        }

        // Restore objects
        logger.info("Unindexing {} objects", objects.size());
        for (Map.Entry<String, JsonElement> member : objects.entrySet()) {
            String name = member.getKey();
            logger.info("Unindexing: '{}'", name);

            // Copy hashed file to target
            String hash = member.getValue().getAsJsonObject().get("hash").getAsString();
            Path source = IndexAssetCommand.resolveObjectFile(inputAssetDir, hash);
            IOHelper.copy(source, outputAssetDir.resolve(name));
        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        logger.info("Asset successfully unindexed: '{}'", inputAssetDirName);
    }
}
