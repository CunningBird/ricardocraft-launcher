package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class PatchAuthlibCommand extends Command {

    private static final Logger logger = LogManager.getLogger(PatchAuthlibCommand.class);

    private transient final DirectoriesManager directoriesManager;

    @Autowired
    public PatchAuthlibCommand(DirectoriesManager directoriesManager) {
        super();
        this.directoriesManager = directoriesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[dir] [authlib file]";
    }

    @Override
    public String getUsageDescription() {
        return "patch client authlib";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        Path dir = directoriesManager.getUpdatesDir().resolve(args[0]);
        Path originalAuthlib;
        if (Files.isDirectory(dir)) {
            Optional<Path> authlibDir = Files.list(dir.resolve("libraries/com/mojang/authlib")).findFirst();
            if (authlibDir.isEmpty()) {
                throw new FileNotFoundException("Directory %s empty or not found".formatted(dir.resolve("com/mojang/authlib")));
            }
            originalAuthlib = Files.list(authlibDir.get()).findFirst().orElseThrow();
        } else {
            originalAuthlib = dir;
        }
        String version = originalAuthlib.getFileName().toString();
        Path launcherAuthlib = Paths.get(args[1]);
        if (Files.isDirectory(launcherAuthlib)) {
            launcherAuthlib = launcherAuthlib.resolve(version.concat(".jar"));
        }
        if (Files.notExists(launcherAuthlib)) {
            throw new FileNotFoundException(launcherAuthlib.toString());
        }
        Path mergedFile = directoriesManager.getTmpDir().resolve("merged.jar");
        logger.info("Merge {} and {} into {}", launcherAuthlib, originalAuthlib, mergedFile);
        try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(mergedFile.toFile()))) {
            Set<String> files = new HashSet<>();
            try (ZipInputStream input = new ZipInputStream(new FileInputStream(launcherAuthlib.toFile()))) {
                ZipEntry entry = input.getNextEntry();
                while (entry != null) {
                    input.transferTo(output);
                    files.add(entry.getName());
                    entry = input.getNextEntry();
                }
            }
            try (ZipInputStream input = new ZipInputStream(new FileInputStream(originalAuthlib.toFile()))) {
                ZipEntry entry = input.getNextEntry();
                while (entry != null) {
                    if (files.contains(entry.getName())) {
                        entry = input.getNextEntry();
                        continue;
                    }
                    input.transferTo(output);
                    files.add(entry.getName());
                    entry = input.getNextEntry();
                }
            }
        }
        logger.info("Delete original authlib {}", originalAuthlib);
        Files.delete(originalAuthlib);
        logger.info("Move {} into {}", mergedFile, originalAuthlib);
        Files.move(mergedFile, originalAuthlib);
        logger.info("Successful");
    }
}
