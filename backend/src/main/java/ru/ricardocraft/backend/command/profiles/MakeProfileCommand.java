package ru.ricardocraft.backend.command.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.MakeProfileHelper;

public class MakeProfileCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    public MakeProfileCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[name] [minecraft version] [dir]";
    }

    @Override
    public String getUsageDescription() {
        return "make profile for any minecraft versions";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 3);
        ClientProfile.Version version = parseClientVersion(args[1]);
        MakeProfileHelper.MakeProfileOption[] options = MakeProfileHelper.getMakeProfileOptionsFromDir(server.updatesDir.resolve(args[2]), version);
        for (MakeProfileHelper.MakeProfileOption option : options) {
            logger.info("Detected option {}", option);
        }
        ClientProfile profile = MakeProfileHelper.makeProfile(version, args[0], options);
        server.config.profileProvider.addProfile(profile);
        logger.info("Profile {} created", args[0]);
        server.syncProfilesDir();
    }
}
