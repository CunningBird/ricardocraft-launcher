package ru.ricardocraft.backend.command.updates.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.UpdatesManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public final class SyncUpdatesCommand extends Command {

    private transient final Logger logger = LogManager.getLogger();

    private transient final UpdatesManager updatesManager;

    @Autowired
    public SyncUpdatesCommand(UpdatesManager updatesManager) {
        super();
        this.updatesManager = updatesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[subdirs...]";
    }

    @Override
    public String getUsageDescription() {
        return "Resync updates dir";
    }

    @Override
    public void invoke(String... args) throws IOException {
        Set<String> dirs = null;
        if (args.length > 0) { // Hash all updates dirs
            dirs = new HashSet<>(args.length);
            Collections.addAll(dirs, args);
        }

        // Hash updates dir
        updatesManager.syncUpdatesDir(dirs);
        logger.info("Updates dir successfully resynced");
    }
}
