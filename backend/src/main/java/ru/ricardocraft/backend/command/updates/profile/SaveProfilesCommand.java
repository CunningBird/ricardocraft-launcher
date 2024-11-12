package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;

import java.util.UUID;

public class SaveProfilesCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    public SaveProfilesCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[profile names...]";
    }

    @Override
    public String getUsageDescription() {
        return "load and save profile";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        if (args.length > 0) {
            for (String profileName : args) {
                ClientProfile profile;
                try {
                    UUID uuid = UUID.fromString(profileName);
                    profile = server.config.profileProvider.getProfile(uuid);
                } catch (IllegalArgumentException ex) {
                    profile = server.config.profileProvider.getProfile(profileName);
                }
                server.config.profileProvider.addProfile(profile);
            }
            server.syncProfilesDir();
        }
    }

}
