package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Component
public final class UnindexAssetCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(UnindexAssetCommand.class);

    private transient final DirectoriesManager directoriesManager;
    private transient final UpdatesManager updatesManager;
    private transient final JacksonManager jacksonManager;

    @Autowired
    public UnindexAssetCommand(DirectoriesManager directoriesManager,
                               UpdatesManager updatesManager,
                               JacksonManager jacksonManager) {
        super();
        this.directoriesManager = directoriesManager;
        this.updatesManager = updatesManager;
        this.jacksonManager = jacksonManager;
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
        Path inputAssetDir = directoriesManager.getUpdatesDir().resolve(inputAssetDirName);
        Path outputAssetDir = directoriesManager.getUpdatesDir().resolve(outputAssetDirName);
        if (outputAssetDir.equals(inputAssetDir))
            throw new CommandException("Indexed and unindexed asset dirs can't be same");

        // Create new asset dir
        logger.info("Creating unindexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Read JSON file
        JsonNode objects;
        logger.info("Reading asset index file: '{}'", indexFileName);
        try (BufferedReader reader = IOHelper.newReader(IndexAssetCommand.resolveIndexFile(inputAssetDir, indexFileName))) {
            objects = jacksonManager.getMapper().readTree(reader).get("objects");
        }

        // Restore objects
        logger.info("Unindexing {} objects", objects.size());
        for (Iterator<Map.Entry<String, JsonNode>> it = objects.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> member = it.next();
            String name = member.getKey();
            logger.info("Unindexing: '{}'", name);

            // Copy hashed file to target
            String hash = member.getValue().get("hash").toString();
            Path source = IndexAssetCommand.resolveObjectFile(inputAssetDir, hash);
            IOHelper.copy(source, outputAssetDir.resolve(name));

        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        logger.info("Asset successfully unindexed: '{}'", inputAssetDirName);
    }
}
