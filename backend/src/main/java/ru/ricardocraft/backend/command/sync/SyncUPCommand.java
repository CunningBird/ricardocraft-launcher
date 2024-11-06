package ru.ricardocraft.backend.command.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;

import java.io.IOException;

public final class SyncUPCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    public SyncUPCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Resync profiles & updates dirs";
    }

    @Override
    public void invoke(String... args) throws IOException {
        server.syncProfilesDir();
        logger.info("Profiles successfully resynced");

        server.syncUpdatesDir(null);
        logger.info("Updates dir successfully resynced");
    }
}
