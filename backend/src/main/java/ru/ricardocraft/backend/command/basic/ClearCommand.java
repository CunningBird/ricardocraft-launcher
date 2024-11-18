package ru.ricardocraft.backend.command.basic;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.base.helper.LogHelper;

@Component
public final class ClearCommand extends Command {
    private final CommandHandler commandHandler;

    public ClearCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        commandHandler.registerCommand("clear", this);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Clear terminal";
    }

    @Override
    public void invoke(String... args) throws Exception {
        commandHandler.clear();
        LogHelper.subInfo("Terminal cleared");
    }
}
