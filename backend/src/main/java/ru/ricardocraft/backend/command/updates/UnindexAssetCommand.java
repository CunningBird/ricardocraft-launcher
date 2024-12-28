package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.base.helper.IOHelper;
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

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public final class UnindexAssetCommand {

    private final DirectoriesManager directoriesManager;
    private final UpdatesManager updatesManager;
    private final JacksonManager jacksonManager;

    @ShellMethod( "[dir] [index] [output-dir] Unindex asset dir (1.7.10+)")
    public void unindexAsset(@ShellOption String indexInputAssetDirName,
                             @ShellOption String indexIndexFileName,
                             @ShellOption String outputOutputAssetDirName) throws Exception {
        String inputAssetDirName = IOHelper.verifyFileName(indexInputAssetDirName);
        String indexFileName = IOHelper.verifyFileName(indexIndexFileName);
        String outputAssetDirName = IOHelper.verifyFileName(outputOutputAssetDirName);
        Path inputAssetDir = directoriesManager.getUpdatesDir().resolve(inputAssetDirName);
        Path outputAssetDir = directoriesManager.getUpdatesDir().resolve(outputAssetDirName);
        if (outputAssetDir.equals(inputAssetDir))
            throw new CommandException("Indexed and unindexed asset dirs can't be same");

        // Create new asset dir
        log.info("Creating unindexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Read JSON file
        JsonNode objects;
        log.info("Reading asset index file: '{}'", indexFileName);
        try (BufferedReader reader = IOHelper.newReader(IndexAssetCommand.resolveIndexFile(inputAssetDir, indexFileName))) {
            objects = jacksonManager.getMapper().readTree(reader).get("objects");
        }

        // Restore objects
        log.info("Unindexing {} objects", objects.size());
        for (Iterator<Map.Entry<String, JsonNode>> it = objects.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> member = it.next();
            String name = member.getKey();
            log.info("Unindexing: '{}'", name);

            // Copy hashed file to target
            String hash = member.getValue().get("hash").toString();
            Path source = IndexAssetCommand.resolveObjectFile(inputAssetDir, hash);
            IOHelper.copy(source, outputAssetDir.resolve(name));

        }

        // Finished
        updatesManager.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        log.info("Asset successfully unindexed: '{}'", inputAssetDirName);
    }
}
