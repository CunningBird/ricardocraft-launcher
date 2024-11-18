package ru.ricardocraft.backend.command.basic;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.command.CommandHandler;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.function.Supplier;

@Component
public final class HelpCommand extends Command {

    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);

    private final CommandHandler commandHandler;

    public HelpCommand(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
        commandHandler.registerCommand("help", this);
    }

    public static void printCommand(String name, Command command) {
        String args = command.getArgsDescription();
        Supplier<String> jansitext = () -> {
            Ansi ansi = new Ansi();
            ansi.fgBright(Ansi.Color.GREEN);
            ansi.a(name + " ");
            ansi.fgBright(Ansi.Color.CYAN);
            ansi.a(args == null ? "[nothing]" : args);
            ansi.reset();
            ansi.a(" - ");
            ansi.fgBright(Ansi.Color.YELLOW);
            ansi.a(command.getUsageDescription());
            ansi.reset();
            return ansi.toString();
        };
        logger.info(jansitext.get());
    }

    public static void printSubCommandsHelp(String base, Command command) {
        command.childCommands.forEach((k, v) -> printCommand(base.concat(" ").concat(k), v));
    }

    public static void printSubCommandsHelp(String name, String[] args, Command command) throws CommandException {
        if (args.length == 0) {
            printSubCommandsHelp(name, command);
        } else {
            Command child = command.childCommands.get(args[0]);
            if (child == null) throw new CommandException(String.format("Unknown sub command: '%s'", args[0]));
            printSubCommandsHelp(name.concat(" ").concat(args[0]), Arrays.copyOfRange(args, 1, args.length), child);
        }
    }

    private static void printCategory(String name, String description) {
        if (description != null) logger.info("Category: {} - {}", name, description);
        else logger.info("Category: {}", name);
    }

    @Override
    public String getArgsDescription() {
        return "[command name]";
    }

    @Override
    public String getUsageDescription() {
        return "Print command usage";
    }

    @Override
    public void invoke(String... args) throws CommandException {
        if (args.length < 1) {
            printCommands();
            return;
        }

        // Print command help
        if (args.length == 1)
            printCommand(args[0]);
        printSubCommandsHelp(args[0], Arrays.copyOfRange(args, 1, args.length), commandHandler.lookup(args[0]));
    }

    private void printCommand(String name) throws CommandException {
        printCommand(name, commandHandler.lookup(name));
    }

    private void printCommands() {
        for (CommandHandler.Category category : commandHandler.getCategories()) {
            printCategory(category.name, category.description);
            for (Entry<String, Command> entry : category.category.commandsMap().entrySet())
                printCommand(entry.getKey(), entry.getValue());
        }
        printCategory("Base", null);
        for (Entry<String, Command> entry : commandHandler.getBaseCategory().commandsMap().entrySet())
            printCommand(entry.getKey(), entry.getValue());

    }
}
