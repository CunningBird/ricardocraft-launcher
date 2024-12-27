package ru.ricardocraft.backend.command.updates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.updates.sync.SyncProfilesCommand;
import ru.ricardocraft.backend.command.updates.sync.SyncUPCommand;
import ru.ricardocraft.backend.command.updates.sync.SyncUpdatesCacheCommand;
import ru.ricardocraft.backend.command.updates.sync.SyncUpdatesCommand;

@Component
public class SyncCommand extends Command {

    @Autowired
    public SyncCommand(SyncProfilesCommand syncProfilesCommand,
                       SyncUpdatesCommand syncUpdatesCommand,
                       SyncUPCommand syncUPCommand,
                       SyncUpdatesCacheCommand syncUpdatesCacheCommand) {
        super();
        this.childCommands.put("profiles", syncProfilesCommand);
        this.childCommands.put("updates", syncUpdatesCommand);
        this.childCommands.put("up", syncUPCommand);
        this.childCommands.put("updatescache", syncUpdatesCacheCommand);
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
