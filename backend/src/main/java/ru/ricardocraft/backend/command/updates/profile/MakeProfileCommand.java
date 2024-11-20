package ru.ricardocraft.backend.command.updates.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.helper.MakeProfileHelper;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

@Component
public class MakeProfileCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(MakeProfileCommand.class);

    private transient final LaunchServerDirectories directories;
    private transient final ProfileProvider profileProvider;
    private transient final JacksonManager jacksonManager;

    @Autowired
    public MakeProfileCommand(LaunchServerDirectories directories,
                              ProfileProvider profileProvider,
                              JacksonManager jacksonManager) {
        super();
        this.directories = directories;
        this.profileProvider = profileProvider;
        this.jacksonManager = jacksonManager;
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
        MakeProfileHelper.MakeProfileOption[] options = MakeProfileHelper.getMakeProfileOptionsFromDir(directories.updatesDir.resolve(args[2]), version);
        for (MakeProfileHelper.MakeProfileOption option : options) {
            logger.info("Detected option {}", option);
        }
        ClientProfile profile = MakeProfileHelper.makeProfile(version, args[0], options);
        profileProvider.addProfile(profile);
        logger.info("Profile {} created", args[0]);
        profileProvider.syncProfilesDir();
    }

    protected ClientProfile.Version parseClientVersion(String arg) throws CommandException, JsonProcessingException {
        if(arg.isEmpty()) {
            throw new CommandException("ClientVersion can't be empty");
        }
        return jacksonManager.getMapper().readValue(arg, ClientProfile.Version.class);
    }
}
