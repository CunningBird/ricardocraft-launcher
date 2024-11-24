package ru.ricardocraft.client.utils.command.basic;

import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.helper.LogHelper;

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
