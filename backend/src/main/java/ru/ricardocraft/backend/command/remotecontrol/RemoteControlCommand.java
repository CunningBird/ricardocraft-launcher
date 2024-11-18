package ru.ricardocraft.backend.command.remotecontrol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

@Component
public class RemoteControlCommand extends Command {

    @Autowired
    public RemoteControlCommand(CommandHandler commandHandler, LaunchServerConfig config) {
        super();
        childCommands.put("list", new ListCommand(config));

        commandHandler.registerCommand("remotecontrol", this);
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
