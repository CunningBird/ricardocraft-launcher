package ru.ricardocraft.client.base.helper.enfs.dir;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Directory extends FileEntry {
    private final Map<String, FileEntry> map = new ConcurrentHashMap<>();

    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    public FileEntry find(Path path) {
        if (path == null) return this;
        Directory current = this;
        FileEntry result = current;
        boolean endMode = false;
        for (Path p : path) {
            if(endMode) return result;
            FileEntry entry = current.get(p.toString());
            if(entry == null) return null;
            if(entry instanceof Directory) {
                current = (Directory) entry;
            } else {
                endMode = true;
            }
            result = entry;
        }
        return result;
    }

    public FileEntry get(String name) {
        return map.get(name);
    }

    public void add(String name, FileEntry entry) {
        map.put(name, entry);
    }

    public void clear() {
        map.clear();
    }

}
