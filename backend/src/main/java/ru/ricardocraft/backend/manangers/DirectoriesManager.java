package ru.ricardocraft.backend.manangers;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.properties.DirectoriesProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Component
public class DirectoriesManager {

    private final Path cacheFile;

    private final Path root;
    private final Path buildDir;
    private final Path tmpDir;

    private final Path updatesDir;
    private final Path updatesAssetsDir;

    private final Path profilesDir;

    private final Path runtimeDir;
    private final Path librariesDir;
    private final Path launcherLibrariesDir;
    private final Path launcherLibrariesCompileDir;
    private final Path launcherPackDir;

    private final Path keyDirectoryDir;
    private final Path trustStoreDir;

    private final Path proguard;

    private final Path mirrorHelperDir;
    private final Path mirrorHelperWorkspaceDir;

    @Autowired
    public DirectoriesManager(DirectoriesProperties properties) throws IOException {
        this.cacheFile = properties.getRoot().resolve(properties.getCacheFile());

        this.root = createDirectoryIfNotExists(properties.getRoot());

        this.buildDir = getDirectory(properties.getRoot(), "build");
        this.tmpDir = getDirectory(properties.getRoot(), "tmp");

        this.profilesDir = getDirectory(properties.getRoot(), "profiles");

        this.runtimeDir = getDirectory(properties.getRoot(), "runtime");
        this.librariesDir = getDirectory(properties.getRoot(), "libraries");
        this.launcherLibrariesDir = getDirectory(properties.getRoot(), "launcher-libraries");
        this.launcherLibrariesCompileDir = getDirectory(properties.getRoot(), "launcher-libraries-compile");
        this.launcherPackDir = getDirectory(properties.getRoot(), "launcherPack");

        this.keyDirectoryDir = getDirectory(properties.getRoot(), ".keys");
        this.trustStoreDir = getDirectory(properties.getRoot(), "truststore");

        this.proguard = getDirectory(properties.getRoot(), "proguard");

        // Updates dir
        this.updatesDir = getDirectory(properties.getRoot(), "updates");
        this.updatesAssetsDir = getDirectory(updatesDir, "assets");

        // MirrorHelper dir
        this.mirrorHelperDir = getDirectory(properties.getRoot(), "mirrorHelper");
        this.mirrorHelperWorkspaceDir = getDirectory(mirrorHelperDir, "workspace");
    }

    private Path getDirectory(Path root, String directoryName) throws IOException {
        Path directoryPath = root.resolve(directoryName);
        return createDirectoryIfNotExists(directoryPath);
    }

    private Path createDirectoryIfNotExists(Path directory) throws IOException {
        if (!IOHelper.isDir(directory)) return Files.createDirectory(directory);
        else return directory;
    }
}
