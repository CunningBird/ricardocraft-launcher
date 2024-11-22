package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.UnpackHelper;
import ru.ricardocraft.backend.binary.JarLauncherInfo;
import ru.ricardocraft.backend.manangers.DirectoriesManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrepareBuildTask implements LauncherBuildTask {

    private transient final Logger logger = LogManager.getLogger(PrepareBuildTask.class);

    private final JarLauncherInfo jarLauncherInfo;
    private final DirectoriesManager directoriesManager;
    private final Path result;

    public PrepareBuildTask(JarLauncherInfo jarLauncherInfo, DirectoriesManager directoriesManager) {
        this.jarLauncherInfo = jarLauncherInfo;
        this.directoriesManager = directoriesManager;
        result = directoriesManager.getBuildDir().resolve("Launcher-clean.jar");
    }

    @Override
    public String getName() {
        return "UnpackFromResources";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        jarLauncherInfo.getCoreLibs().clear();
        jarLauncherInfo.getAddonLibs().clear();
        jarLauncherInfo.getFiles().clear();
        IOHelper.walk(directoriesManager.getLibrariesDir(), new ListFileVisitor(jarLauncherInfo.getCoreLibs()), false);
        if(Files.isDirectory(directoriesManager.getLauncherLibrariesCompileDir())) {
            IOHelper.walk(directoriesManager.getLauncherLibrariesCompileDir(), new ListFileVisitor(jarLauncherInfo.getAddonLibs()), false);
        }
        try(Stream<Path> stream = Files.walk(directoriesManager.getLauncherPackDir(), FileVisitOption.FOLLOW_LINKS).filter((e) -> {
            try {
                return !Files.isDirectory(e) && !Files.isHidden(e);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        })) {
            var map = stream.collect(Collectors.toMap(k -> directoriesManager.getLauncherPackDir().relativize(k).toString().replace("\\", "/"), (v) -> v));
            jarLauncherInfo.getFiles().putAll(map);
        }
        UnpackHelper.unpack(IOHelper.getResourceURL("Launcher.jar"), result);
        tryUnpack();
        return result;
    }

    public void tryUnpack() throws IOException {
        logger.info("Unpacking launcher native guard list and runtime");
        UnpackHelper.unpackZipNoCheck("runtime.zip", directoriesManager.getRuntimeDir());
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
