package ru.ricardocraft.backend.service.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.UpdatesService;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public final class UnindexAssetService {

    private final DirectoriesService directoriesService;
    private final UpdatesService updatesService;
    private final ObjectMapper objectMapper;

    public void unindexAsset(String indexInputAssetDirName, String indexIndexFileName, String outputOutputAssetDirName) throws Exception {
        String inputAssetDirName = IOHelper.verifyFileName(indexInputAssetDirName);
        String indexFileName = IOHelper.verifyFileName(indexIndexFileName);
        String outputAssetDirName = IOHelper.verifyFileName(outputOutputAssetDirName);
        Path inputAssetDir = directoriesService.getUpdatesDir().resolve(inputAssetDirName);
        Path outputAssetDir = directoriesService.getUpdatesDir().resolve(outputAssetDirName);
        if (outputAssetDir.equals(inputAssetDir))
            throw new Exception("Indexed and unindexed asset dirs can't be same");

        // Create new asset dir
        log.info("Creating unindexed asset dir: '{}'", outputAssetDirName);
        Files.createDirectory(outputAssetDir);

        // Read JSON file
        JsonNode objects;
        log.info("Reading asset index file: '{}'", indexFileName);
        try (BufferedReader reader = IOHelper.newReader(IndexAssetService.resolveIndexFile(inputAssetDir, indexFileName))) {
            objects = objectMapper.readTree(reader).get("objects");
        }

        // Restore objects
        log.info("Unindexing {} objects", objects.size());
        for (Iterator<Map.Entry<String, JsonNode>> it = objects.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> member = it.next();
            String name = member.getKey();
            log.info("Unindexing: '{}'", name);

            // Copy hashed file to target
            String hash = member.getValue().get("hash").toString();
            Path source = IndexAssetService.resolveObjectFile(inputAssetDir, hash);
            IOHelper.copy(source, outputAssetDir.resolve(name));

        }

        // Finished
        updatesService.syncUpdatesDir(Collections.singleton(outputAssetDirName));
        log.info("Asset successfully unindexed: '{}'", inputAssetDirName);
    }
}
