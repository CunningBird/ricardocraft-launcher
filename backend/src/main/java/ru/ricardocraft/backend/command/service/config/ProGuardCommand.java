package ru.ricardocraft.backend.command.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.service.config.proguard.ProGuardCleanCommand;
import ru.ricardocraft.backend.command.service.config.proguard.ProGuardRegenCommand;
import ru.ricardocraft.backend.command.service.config.proguard.ProGuardResetCommand;

@Component
public class ProGuardCommand extends Command {

    @Autowired
    public ProGuardCommand(ProGuardCleanCommand cleanCommand,
                           ProGuardRegenCommand regenCommand,
                           ProGuardResetCommand resetCommand) {
        super();
        this.childCommands.put("clean", cleanCommand);
        this.childCommands.put("regen", regenCommand);
        this.childCommands.put("reset", resetCommand);
    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args...]";
    }

    @Override
    public String getUsageDescription() {
        return "manage proGuard";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
