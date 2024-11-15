package ru.ricardocraft.backend.command.updates.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.UpdatesManager;

import java.io.IOException;

@Component
public final class SyncUPCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    private transient final ProfileProvider profileProvider;
    private transient final UpdatesManager updatesManager;

    @Autowired
    public SyncUPCommand(ProfileProvider profileProvider,
                         UpdatesManager updatesManager) {
        super();
        this.profileProvider = profileProvider;
        this.updatesManager = updatesManager;
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
        profileProvider.syncProfilesDir();
        logger.info("Profiles successfully resynced");

        updatesManager.syncUpdatesDir(null);
        logger.info("Updates dir successfully resynced");
    }
}
