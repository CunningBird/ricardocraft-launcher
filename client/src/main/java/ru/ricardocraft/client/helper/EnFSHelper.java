package ru.ricardocraft.client.helper;

import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.LauncherConfig;
import ru.ricardocraft.client.helper.enfs.EnFS;
import ru.ricardocraft.client.helper.enfs.dir.CachedFile;
import ru.ricardocraft.client.helper.enfs.dir.FileEntry;
import ru.ricardocraft.client.helper.enfs.dir.RealFile;
import ru.ricardocraft.client.helper.enfs.dir.URLFile;
import ru.ricardocraft.client.utils.RuntimeCryptedFile;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class EnFSHelper {

    private static final Set<String> themesCached = new HashSet<>(1);
    private static final String BASE_DIRECTORY = "tgui";
    private static final EnFS enfs = new EnFS();

    public static EnFS getEnFS() {
        return enfs;
    }

    public static void initEnFS() throws IOException {
        enfs.newDirectory(Paths.get(BASE_DIRECTORY));
        if (LogHelper.isDevEnabled()) {
            EnFS.DEBUG_OUTPUT = new LauncherEnFsDebugOutput();
        }
    }

    private static class LauncherEnFsDebugOutput implements EnFS.DebugOutput {

        @Override
        public void debug(String str) {
            LogHelper.debug(str);
        }

        @Override
        public void debug(String format, Object... args) {
            LogHelper.debug(format, args);
        }
    }

    public static Path initEnFSDirectory(LauncherConfig config, String theme, Path realDirectory) throws IOException {
        String themeName = theme != null ? theme : "common";
        Path enfsDirectory = Paths.get(BASE_DIRECTORY, themeName);
        if (themesCached.contains(themeName)) {
            return enfsDirectory;
        }
        Path basePath = realDirectory == null ? Path.of("") : realDirectory;
        Path themePath = basePath.resolve("themes").resolve(themeName);
        // Add theme files to themePaths
        if (realDirectory != null) {
            Set<Path> themePaths = new HashSet<>();
            Path relThemePath = realDirectory.relativize(themePath);
            try (var stream = Files.walk(realDirectory)) {
                stream.forEach(f -> {
                    if (Files.isDirectory(f)) {
                        return;
                    }
                    Path real = realDirectory.relativize(f);
                    if (f.startsWith(themePath)) {
                        themePaths.add(relThemePath.relativize(real));
                    }
                });
            }
            try (var stream = Files.walk(realDirectory)) {
                stream.forEach(f -> {
                    if (Files.isDirectory(f)) {
                        return;
                    }
                    Path real = realDirectory.relativize(f);
                    if (themePaths.contains(real)) {
                        f = themePath.resolve(real);
                        themePaths.remove(real);
                    }
                    try {
                        Path path = enfsDirectory.resolve(real);
                        enfs.newDirectories(path.getParent());
                        FileEntry entry = new RealFile(f);
                        enfs.addFile(path, entry);
                    } catch (IOException e) {
                        LogHelper.error(e);
                    }
                });
            }
        } else {
            Set<String> themePaths = new HashSet<>();
            String themePathString = String.format("themes/%s/", themeName);
            config.runtime.forEach((f, digest) -> {
                if (f.startsWith(themePathString)) {
                    themePaths.add(f.substring(themePathString.length()));
                }
            });
            config.runtime.forEach((f, digest) -> {
                String real = f;
                if (themePaths.contains(real)) {
                    f = themePathString.concat(real);
                    digest = config.runtime.get(f);
                    themePaths.remove(real);
                    LogHelper.dev("Replace %s to %s", real, f);
                }
                try {
                    Path path = enfsDirectory.resolve(real);
                    enfs.newDirectories(path.getParent());
                    FileEntry entry = makeFile(config, f, digest);
                    LogHelper.dev("makeFile %s (%s) to %s", f, SecurityHelper.toHex(digest), path.toString());
                    enfs.addFile(path, entry);
                } catch (IOException e) {
                    LogHelper.error(e);
                }
            });
        }
        return enfsDirectory;
    }

    private static FileEntry makeFile(LauncherConfig config, String name, byte[] digest) throws IOException {
        FileEntry entry;
        if (config.runtimeEncryptKey == null) {
            entry = new URLFile(Launcher.getResourceURL(name));
        } else {
            String encodedName = "runtime/" + SecurityHelper.toHex(digest);
            entry = new CachedFile(new RuntimeCryptedFile(() -> {
                try {
                    return IOHelper.newInput(IOHelper.getResourceURL(encodedName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, SecurityHelper.fromHex(config.runtimeEncryptKey)));
        }
        return entry;
    }

    public static URL getURL(String name) throws IOException {
        try (InputStream stream = enfs.getInputStream(Paths.get(name))) {
            return new URI("enfs", null, "/" + name, null).toURL();
        } catch (UnsupportedOperationException ex) {
            throw new FileNotFoundException(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
