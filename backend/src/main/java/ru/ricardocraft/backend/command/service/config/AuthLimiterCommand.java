package ru.ricardocraft.backend.command.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.service.config.authLimiter.*;

@Component
public class AuthLimiterCommand extends Command {

    @Autowired
    public AuthLimiterCommand(AuthLimiterGcCommand authLimiterGcCommand,
                              AuthLimiterClearCommand authLimiterClearCommand,
                              AuthLimiterAddExcludeCommand authLimiterAddExcludeCommand,
                              AuthLimiterRmExcludeCommand authLimiterRmExcludeCommand,
                              AuthLimiterClearExcludeCommand authLimiterClearExcludeCommand) {
        super();
        this.childCommands.put("gc", authLimiterGcCommand);
        this.childCommands.put("clear", authLimiterClearCommand);
        this.childCommands.put("addExclude", authLimiterAddExcludeCommand);
        this.childCommands.put("rmExclude", authLimiterRmExcludeCommand);
        this.childCommands.put("clearExclude", authLimiterClearExcludeCommand);
    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args...]";
    }

    @Override
    public String getUsageDescription() {
        return "manage authLimiter";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
