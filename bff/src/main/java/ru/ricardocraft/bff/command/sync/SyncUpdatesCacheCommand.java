package ru.ricardocraft.bff.command.sync;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

public class SyncUpdatesCacheCommand extends Command {
    public SyncUpdatesCacheCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "sync updates cache";
    }

    @Override
    public void invoke(String... args) throws Exception {
        server.updatesManager.readUpdatesFromCache();
    }
}
