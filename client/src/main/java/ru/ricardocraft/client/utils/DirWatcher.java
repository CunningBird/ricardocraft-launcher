package ru.ricardocraft.client.utils;

import ru.ricardocraft.client.core.hasher.FileNameMatcher;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedFile;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.JVMHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public final class DirWatcher implements Runnable, AutoCloseable {
    public static final boolean FILE_TREE_SUPPORTED = JVMHelper.OS_TYPE == JVMHelper.OS.WINDOWS;
    public static final String IGN_OVERFLOW = "launcher.dirwatcher.ignoreOverflows";
    // Constants
    private static final Kind<?>[] KINDS = {
            StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE
    };
    private static final boolean PROP_IGN_OVERFLOW = Boolean.parseBoolean(System.getProperty(IGN_OVERFLOW, "true"));
    // Instance
    private final Path dir;
    private final HashedDir hdir;
    private final FileNameMatcher matcher;
    private final WatchService service;
    private final boolean digest;

    private final SettingsManager settings;

    public DirWatcher(SettingsManager settingsManager, Path dir, HashedDir hdir, FileNameMatcher matcher, boolean digest) throws IOException {
        this.settings = settingsManager;
        this.dir = Objects.requireNonNull(dir, "dir");
        this.hdir = Objects.requireNonNull(hdir, "hdir");
        this.matcher = matcher;
        this.digest = digest;
        service = dir.getFileSystem().newWatchService();

        // Register dirs recursively
        IOHelper.walk(dir, new RegisterFileVisitor(), true);
        LogHelper.subInfo("DirWatcher %s", dir.toString());
    }

    private void handleError(Throwable e) {
        LogHelper.error(e);
        settings.exitLauncher(-123);
    }

    private static Deque<String> toPath(Iterable<Path> path) {
        Deque<String> result = new LinkedList<>();
        for (Path pe : path)
            result.add(pe.toString());
        return result;
    }

    @Override
    public void close() throws IOException {
        service.close();
    }

    private void processKey(WatchKey key) throws IOException {
        Path watchDir = (Path) key.watchable();
        for (WatchEvent<?> event : key.pollEvents()) {
            Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                if (PROP_IGN_OVERFLOW)
                    continue; // Sometimes it's better to ignore than interrupt fair playing
                throw new IOException("Overflow");
            }

            // Resolve paths and verify is not exclusion
            Path path = watchDir.resolve((Path) event.context());
            Deque<String> stringPath = toPath(dir.relativize(path));
            if (matcher != null && !matcher.shouldVerify(stringPath))
                continue; // Exclusion; should not be verified
            // Verify is REALLY modified (not just attributes)
            if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                HashedEntry entry = hdir.resolve(stringPath);
                if (entry != null && (entry.getType() != HashedEntry.Type.FILE || ((HashedFile) entry).isSame(path, digest)))
                    continue; // Modified attributes, not need to worry :D
            }

            // Forbidden modification!
            throw new SecurityException(String.format("Forbidden modification (%s, %d times): '%s'", kind, event.count(), path));
        }
        key.reset();
    }

    private void processLoop() throws IOException, InterruptedException {
        LogHelper.debug("WatchService start processing");
        while (!Thread.interrupted())
            processKey(service.take());
        LogHelper.debug("WatchService closed");
    }

    @Override

    public void run() {
        try {
            processLoop();
        } catch (InterruptedException | ClosedWatchServiceException ignored) {
            LogHelper.debug("WatchService closed 2");
            // Do nothing (closed etc)
        } catch (Throwable exc) {
            handleError(exc);
        }
    }

    private final class RegisterFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult result = super.preVisitDirectory(dir, attrs);
            dir.register(service, KINDS);
            return result;
        }
    }
}
