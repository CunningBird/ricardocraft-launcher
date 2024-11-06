package ru.ricardocraft.backend.command.profiles;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;

public class ProfilesCommand extends Command {
    public ProfilesCommand(LaunchServer server) {
        super(server);
        this.childCommands.put("make", new MakeProfileCommand(server));
        this.childCommands.put("save", new SaveProfilesCommand(server));
        this.childCommands.put("clone", new CloneProfileCommand(server));
        this.childCommands.put("list", new ListProfilesCommand(server));
        this.childCommands.put("delete", new DeleteProfileCommand(server));
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
