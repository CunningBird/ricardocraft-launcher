package ru.ricardocraft.client.commands.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.commands.Command;
import ru.ricardocraft.client.commands.CommandHandler;
import ru.ricardocraft.client.helper.LogHelper;

@Component
public final class ClearCommand extends Command {
    private final CommandHandler handler;

    @Autowired
    public ClearCommand(CommandHandler handler) {
        this.handler = handler;
        handler.registerCommand("clear", this);
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
