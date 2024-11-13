package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.util.UUID;

@Component
public class DeleteProfileCommand extends Command {

    private final transient Logger logger = LogManager.getLogger(ListProfilesCommand.class);

    private final transient LaunchServerConfig config;

    @Autowired
    public DeleteProfileCommand(LaunchServerConfig config) {
        super();
        this.config = config;
    }

    @Override
    public String getArgsDescription() {
        return "[uuid/title]";
    }

    @Override
    public String getUsageDescription() {
        return "permanently delete profile";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        ClientProfile profile;
        try {
            UUID uuid = UUID.fromString(args[0]);
            profile = config.profileProvider.getProfile(uuid);
        } catch (IllegalArgumentException ex) {
            profile = config.profileProvider.getProfile(args[0]);
        }
        if (profile == null) {
            logger.error("Profile {} not found", args[0]);
            return;
        }
        logger.warn("THIS ACTION DELETE PROFILE AND ALL FILES IN {}", profile.getDir());

        logger.info("Delete {} ({})", profile.getTitle(), profile.getUUID());
        config.profileProvider.deleteProfile(profile);
        logger.info("Delete {}", profile.getDir());
        config.updatesProvider.delete(profile.getDir());
    }
}
