package ru.ricardocraft.backend.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.DirectoriesService;

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

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class PatchAuthlibCommand {

    private final DirectoriesService directoriesService;

    @ShellMethod("[dir] [authlib file] patch client authlib")
    public void patchAuthlib(@ShellOption String patchDirectory,
                             @ShellOption String authlibFile) throws Exception {
        Path dir = directoriesService.getUpdatesDir().resolve(patchDirectory);
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
        Path launcherAuthlib = Paths.get(authlibFile);
        if (Files.isDirectory(launcherAuthlib)) {
            launcherAuthlib = launcherAuthlib.resolve(version.concat(".jar"));
        }
        if (Files.notExists(launcherAuthlib)) {
            throw new FileNotFoundException(launcherAuthlib.toString());
        }
        Path mergedFile = directoriesService.getTmpDir().resolve("merged.jar");
        log.info("Merge {} and {} into {}", launcherAuthlib, originalAuthlib, mergedFile);
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
        log.info("Delete original authlib {}", originalAuthlib);
        Files.delete(originalAuthlib);
        log.info("Move {} into {}", mergedFile, originalAuthlib);
        Files.move(mergedFile, originalAuthlib);
        log.info("Successful");
    }
}
