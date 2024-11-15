package ru.ricardocraft.backend.auth.updates;

import ru.ricardocraft.backend.core.hasher.HashedDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class UpdatesProvider {

    public void init() {
    }

    public void sync() throws IOException {
        sync(null);
    }

    public abstract void syncInitially() throws IOException;

    public abstract void sync(Collection<String> updateNames) throws IOException;

    public abstract HashedDir getUpdatesDir(String updateName);

    public abstract void upload(String updateName, Map<String, Path> files, boolean deleteAfterUpload) throws IOException;

    public abstract Map<String, Path> download(String updateName, List<String> files);

    public abstract void delete(String updateName, List<String> files) throws IOException;

    public abstract void delete(String updateName) throws IOException;

    public abstract void create(String updateName) throws IOException;
}
