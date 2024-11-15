package ru.ricardocraft.backend.command.updates.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.command.Command;

import java.io.IOException;

@Component
public final class SyncProfilesCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    private transient final ProfileProvider profileProvider;

    public SyncProfilesCommand(ProfileProvider profileProvider) {
        super();
        this.profileProvider = profileProvider;
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
        profileProvider.syncProfilesDir();
        logger.info("Profiles successfully resynced");
    }
}
