package ru.ricardocraft.backend.command.updates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.UpdatesManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public class SyncCommand {

    private final ProfileProvider profileProvider;
    private final UpdatesManager updatesManager;

    @ShellMethod("Resync profiles dir")
    public void syncProfiles() throws IOException {
        profileProvider.syncProfilesDir();
        log.info("Profiles successfully resynced");
    }

    @ShellMethod("sync updates cache")
    public void syncUpdatesCache() throws Exception {
        updatesManager.readUpdatesFromCache();
    }

    @ShellMethod("Resync profiles & updates dirs")
    public void syncUp() throws IOException {
        profileProvider.syncProfilesDir();
        log.info("Profiles successfully resynced");

        updatesManager.syncUpdatesDir(null);
        log.info("Updates dir successfully resynced");
    }

    @ShellMethod("[subdirs...] Resync updates dir")
    public void syncUpdates(@ShellOption String... args) throws IOException {
        Set<String> dirs = null;
        if (args.length > 0) { // Hash all updates dirs
            dirs = new HashSet<>(args.length);
            Collections.addAll(dirs, args);
        }

        // Hash updates dir
        updatesManager.syncUpdatesDir(dirs);
        log.info("Updates dir successfully resynced");
    }
}
