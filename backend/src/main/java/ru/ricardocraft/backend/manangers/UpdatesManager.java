package ru.ricardocraft.backend.manangers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.core.hasher.HashedDir;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class UpdatesManager {

    private final UpdatesProvider updatesProvider;

    @Deprecated
    public void readUpdatesFromCache() {

    }

    @Deprecated
    public void syncUpdatesDir(Collection<String> dirs) throws IOException {
        updatesProvider.sync(dirs);
    }

    @Deprecated
    public HashSet<String> getUpdatesList() {
        return new HashSet<>();
    }

    @Deprecated
    public HashedDir getUpdate(String name) {
        return updatesProvider.getUpdatesDir(name);
    }

    @Deprecated
    public void addUpdate(String name, HashedDir dir) {
        throw new UnsupportedOperationException();
    }
}
