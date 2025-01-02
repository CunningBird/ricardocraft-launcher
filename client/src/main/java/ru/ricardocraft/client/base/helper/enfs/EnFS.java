package ru.ricardocraft.client.base.helper.enfs;

import ru.ricardocraft.client.base.helper.enfs.dir.Directory;
import ru.ricardocraft.client.base.helper.enfs.dir.FileEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class EnFS {

    public static final DebugOutput NULL_DEBUG_OUTPUT = new DebugOutput() {
        @Override
        public void debug(String str) {

        }

        @Override
        public void debug(String format, Object... args) {

        }
    };
    public static volatile DebugOutput DEBUG_OUTPUT = NULL_DEBUG_OUTPUT;
    private final Directory root = new Directory();

    public FileEntry getFile(Path path) {
        DEBUG_OUTPUT.debug("Open %s", path.toString());
        return root.find(path);
    }

    public void newDirectory(Path path) throws IOException {
        DEBUG_OUTPUT.debug("newDirectory %s", path.toString());
        addFile(path, new Directory());
    }

    public void newDirectories(Path path) {
        if (path == null) return;
        DEBUG_OUTPUT.debug("newDirectories %s", path.toString());
        Directory current = root;
        for (Path p : path) {
            Directory entity = (Directory) current.get(p.toString());
            if (entity == null) {
                entity = new Directory();
                current.add(p.toString(), entity);
            }
            current = entity;
        }
    }

    public InputStream getInputStream(Path path) throws IOException {
        DEBUG_OUTPUT.debug("getInputStream %s", path.toString());
        FileEntry entry = root.find(path);
        if (entry == null) throw new FileNotFoundException(String.format("File %s not found", path));
        return entry.getInputStream();
    }

    public void addFile(Path path, FileEntry file) throws IOException {
        FileEntry entry = root.find(path.getParent());
        if (entry == null) throw new FileNotFoundException(String.format("Directory %s not found", path.getParent()));
        if (entry instanceof Directory) {
            ((Directory) entry).add(path.getFileName().toString(), file);
        } else {
            throw new IOException(String.format("%s is not directory", path.getParent()));
        }
    }

    public void clear() {
        root.clear();
    }

    public interface DebugOutput {
        void debug(String str);

        void debug(String format, Object... args);
    }
}
