package ru.ricardocraft.backend.command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.ReconfigurableManager;

@Component
public class ConfigCommand extends Command {

    @Autowired
    public ConfigCommand(ReconfigurableManager reconfigurableManager) {
        super(reconfigurableManager.getCommands());
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
