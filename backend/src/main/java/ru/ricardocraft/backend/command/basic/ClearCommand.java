package ru.ricardocraft.backend.command.basic;

import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.helper.LogHelper;

public final class ClearCommand extends Command {
    private final CommandHandler handler;

    public ClearCommand(CommandHandler handler) {
        this.handler = handler;
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
        handler.clear();
        LogHelper.subInfo("Terminal cleared");
    }
}
