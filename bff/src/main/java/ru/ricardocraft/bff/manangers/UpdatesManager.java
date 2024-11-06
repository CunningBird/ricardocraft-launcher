package ru.ricardocraft.bff.manangers;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.core.hasher.HashedDir;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class UpdatesManager {
    private final LaunchServer server;

    public UpdatesManager(LaunchServer server) {
        this.server = server;
    }

    @Deprecated
    public void readUpdatesFromCache() throws IOException {

    }

    @Deprecated
    public void readUpdatesDir() throws IOException {

    }

    @Deprecated
    public void syncUpdatesDir(Collection<String> dirs) throws IOException {
        server.config.updatesProvider.sync(dirs);
    }

    @Deprecated
    public HashSet<String> getUpdatesList() {
        return new HashSet<>();
    }

    @Deprecated
    public HashedDir getUpdate(String name) {
        return server.config.updatesProvider.getUpdatesDir(name);
    }

    @Deprecated
    public void addUpdate(String name, HashedDir dir) {
        throw new UnsupportedOperationException();
    }
}
