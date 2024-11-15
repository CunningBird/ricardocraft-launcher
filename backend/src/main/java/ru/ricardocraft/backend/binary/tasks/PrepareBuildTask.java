package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.UnpackHelper;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrepareBuildTask implements LauncherBuildTask {
    private final JARLauncherBinary launcherBinary;
    private final LaunchServerDirectories directories;
    private final Path result;
    private transient final Logger logger = LogManager.getLogger();

    public PrepareBuildTask(JARLauncherBinary launcherBinary, LaunchServerDirectories directories) {
        this.launcherBinary = launcherBinary;
        this.directories = directories;
        result = launcherBinary.buildDirectory.resolve("Launcher-clean.jar");
    }

    @Override
    public String getName() {
        return "UnpackFromResources";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        launcherBinary.coreLibs.clear();
        launcherBinary.addonLibs.clear();
        launcherBinary.files.clear();
        IOHelper.walk(directories.launcherLibrariesDir, new ListFileVisitor(launcherBinary.coreLibs), false);
        if(Files.isDirectory(directories.launcherLibrariesCompileDir)) {
            IOHelper.walk(directories.launcherLibrariesCompileDir, new ListFileVisitor(launcherBinary.addonLibs), false);
        }
        try(Stream<Path> stream = Files.walk(directories.launcherPackDir, FileVisitOption.FOLLOW_LINKS).filter((e) -> {
            try {
                return !Files.isDirectory(e) && !Files.isHidden(e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        })) {
            var map = stream.collect(Collectors.toMap(k -> directories.launcherPackDir.relativize(k).toString().replace("\\", "/"), (v) -> v));
            launcherBinary.files.putAll(map);
        }
        UnpackHelper.unpack(IOHelper.getResourceURL("Launcher.jar"), result);
        tryUnpack();
        return result;
    }

    public void tryUnpack() throws IOException {
        logger.info("Unpacking launcher native guard list and runtime");
        UnpackHelper.unpackZipNoCheck("runtime.zip", launcherBinary.runtimeDir);
    }

    private static final class ListFileVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> lst;

        private ListFileVisitor(List<Path> lst) {
            this.lst = lst;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isDirectory(file) && file.toFile().getName().endsWith(".jar"))
                lst.add(file);
            return super.visitFile(file, attrs);
        }
    }
}
