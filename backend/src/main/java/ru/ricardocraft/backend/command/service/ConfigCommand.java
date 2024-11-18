package ru.ricardocraft.backend.command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.service.config.AuthLimiterCommand;
import ru.ricardocraft.backend.command.service.config.AuthProviderCommand;
import ru.ricardocraft.backend.command.service.config.ProGuardCommand;

@Component
public class ConfigCommand extends Command {

    @Autowired
    public ConfigCommand(AuthProviderCommand authProviderCommand,
                         AuthLimiterCommand authLimiterCommand,
                         ProGuardCommand proGuardCommand) {
        super();
        this.childCommands.put("authprovider", authProviderCommand);
        this.childCommands.put("authLimiter", authLimiterCommand);
        this.childCommands.put("proguard", proGuardCommand);
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
