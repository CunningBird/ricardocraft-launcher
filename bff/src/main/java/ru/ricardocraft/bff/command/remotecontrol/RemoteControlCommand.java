package ru.ricardocraft.bff.command.remotecontrol;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

public class RemoteControlCommand extends Command {
    public RemoteControlCommand(LaunchServer server) {
        super(server);
        childCommands.put("list", new ListCommand(server));
    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "Manage RemoteControl module";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
