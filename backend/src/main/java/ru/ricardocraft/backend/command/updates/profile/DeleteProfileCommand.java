package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.profiles.ClientProfile;

import java.util.UUID;

@Component
public class DeleteProfileCommand extends Command {

    private final Logger logger = LogManager.getLogger(DeleteProfileCommand.class);

    private final transient UpdatesProvider updatesProvider;
    private final transient ProfileProvider profileProvider;

    @Autowired
    public DeleteProfileCommand(UpdatesProvider updatesProvider, ProfileProvider profileProvider) {
        super();
        this.updatesProvider = updatesProvider;
        this.profileProvider = profileProvider;
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
            profile = profileProvider.getProfile(uuid);
        } catch (IllegalArgumentException ex) {
            profile = profileProvider.getProfile(args[0]);
        }
        if (profile == null) {
            logger.error("Profile {} not found", args[0]);
            return;
        }
        logger.warn("THIS ACTION DELETE PROFILE AND ALL FILES IN {}", profile.getDir());

        logger.info("Delete {} ({})", profile.getTitle(), profile.getUUID());
        profileProvider.deleteProfile(profile);
        logger.info("Delete {}", profile.getDir());
        updatesProvider.delete(profile.getDir());
    }
}
