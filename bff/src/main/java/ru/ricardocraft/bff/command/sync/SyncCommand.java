package ru.ricardocraft.bff.command.sync;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

public class SyncCommand extends Command {
    public SyncCommand(LaunchServer server) {
        super(server);
        this.childCommands.put("profiles", new SyncProfilesCommand(server));
        this.childCommands.put("binaries", new SyncBinariesCommand(server));
        this.childCommands.put("updates", new SyncUpdatesCommand(server));
        this.childCommands.put("up", new SyncUPCommand(server));
        this.childCommands.put("updatescache", new SyncUpdatesCacheCommand(server));
    }

    @Override
    public String getArgsDescription() {
        return "[updates/profiles/up/binaries/launchermodules/updatescache] [args...]";
    }

    @Override
    public String getUsageDescription() {
        return "sync specified objects";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
