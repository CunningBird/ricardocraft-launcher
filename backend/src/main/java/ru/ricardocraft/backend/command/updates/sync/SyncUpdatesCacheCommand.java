package ru.ricardocraft.backend.command.updates.sync;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.UpdatesManager;

@Component
public class SyncUpdatesCacheCommand extends Command {

    private transient final UpdatesManager updatesManager;

    @Autowired
    public SyncUpdatesCacheCommand(UpdatesManager updatesManager) {
        super();
        this.updatesManager = updatesManager;
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
        updatesManager.readUpdatesFromCache();
    }
}
