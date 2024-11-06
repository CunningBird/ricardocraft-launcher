package ru.ricardocraft.bff.command.service;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

public class ConfigCommand extends Command {
    public ConfigCommand(LaunchServer server) {
        super(server.reconfigurableManager.getCommands(), server);
    }

    @Override
    public String getArgsDescription() {
        return "[name] [action] [more args]";
    }

    @Override
    public String getUsageDescription() {
        return "call reconfigurable action";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
