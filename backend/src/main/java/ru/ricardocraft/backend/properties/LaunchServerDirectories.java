package ru.ricardocraft.backend.properties;

import ru.ricardocraft.backend.helper.SecurityHelper;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LaunchServerDirectories {
    public static final String UPDATES_NAME = "config/updates",
            TRUSTSTORE_NAME = "config/truststore",
            LAUNCHERLIBRARIES_NAME = "config/launcher-libraries",
            LAUNCHERLIBRARIESCOMPILE_NAME = "config/launcher-libraries-compile",
            LAUNCHERPACK_NAME = "config/launcher-pack",
            KEY_NAME = "config/.keys",
            LIBRARIES = "config/libraries";
    public Path updatesDir;
    public Path librariesDir;
    public Path launcherLibrariesDir;
    public Path launcherLibrariesCompileDir;
    public Path launcherPackDir;
    public Path keyDirectory;
    public Path dir;
    public Path trustStore;
    public Path tmpDir;

    public void collect() {
        if (updatesDir == null) updatesDir = getPath(UPDATES_NAME);
        if (trustStore == null) trustStore = getPath(TRUSTSTORE_NAME);
        if (launcherLibrariesDir == null) launcherLibrariesDir = getPath(LAUNCHERLIBRARIES_NAME);
        if (launcherLibrariesCompileDir == null)
            launcherLibrariesCompileDir = getPath(LAUNCHERLIBRARIESCOMPILE_NAME);
        if (launcherPackDir == null)
            launcherPackDir = getPath(LAUNCHERPACK_NAME);
        if (keyDirectory == null) keyDirectory = getPath(KEY_NAME);
        if (librariesDir == null) librariesDir = getPath(LIBRARIES);
        if (tmpDir == null)
            tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("launchserver-%s".formatted(SecurityHelper.randomStringToken()));
    }

    private Path getPath(String dirName) {
        String property = System.getProperty("launchserver.dir." + dirName, null);
        if (property == null) return dir.resolve(dirName);
        else return Paths.get(property);
    }
}