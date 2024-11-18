package ru.ricardocraft.backend.command.updates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.updates.profile.*;

@Component
public class ProfilesCommand extends Command {

    @Autowired
    public ProfilesCommand(MakeProfileCommand makeProfileCommand,
                           SaveProfilesCommand saveProfilesCommand,
                           CloneProfileCommand cloneProfileCommand,
                           ListProfilesCommand listProfilesCommand,
                           DeleteProfileCommand deleteProfileCommand) {
        super();
        this.childCommands.put("make", makeProfileCommand);
        this.childCommands.put("save", saveProfilesCommand);
        this.childCommands.put("clone", cloneProfileCommand);
        this.childCommands.put("list", listProfilesCommand);
        this.childCommands.put("delete", deleteProfileCommand);
    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args...]";
    }

    @Override
    public String getUsageDescription() {
        return "manage profiles";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
