package ru.ricardocraft.backend.command.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandHandler;

@Component
public final class ClearCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(ClearCommand.class);

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
        logger.info("Terminal cleared");
    }
}
