package ru.ricardocraft.backend.command.service;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;

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
