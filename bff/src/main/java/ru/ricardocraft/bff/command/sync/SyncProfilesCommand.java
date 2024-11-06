package ru.ricardocraft.bff.command.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

import java.io.IOException;

public final class SyncProfilesCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    public SyncProfilesCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Resync profiles dir";
    }

    @Override
    public void invoke(String... args) throws IOException {
        server.syncProfilesDir();
        logger.info("Profiles successfully resynced");
    }
}
