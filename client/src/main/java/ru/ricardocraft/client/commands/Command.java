package ru.ricardocraft.client.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Command {
    /**
     * List of available subcommands
     */
    public final Map<String, Command> childCommands;

    public Command() {
        childCommands = new HashMap<>();
    }

    public Command(Map<String, Command> childCommands) {
        this.childCommands = childCommands;
    }


    public abstract String getArgsDescription(); // "<required> [optional]"


    public abstract String getUsageDescription();

    /**
     * Transfer control to subcommands
     *
     * @param args command arguments(includes subcommand name)
     * @throws Exception Error executing command
     */
    public void invokeSubcommands(String... args) throws Exception {
        verifyArgs(args, 1);
        Command command = childCommands.get(args[0]);
        if (command == null) throw new CommandException(String.format("Unknown sub command: '%s'", args[0]));
        command.invoke(Arrays.copyOfRange(args, 1, args.length));
    }

    /**
     * Run current command
     *
     * @param args command arguments
     * @throws Exception Error executing command
     */
    public abstract void invoke(String... args) throws Exception;


    protected final void verifyArgs(String[] args, int min) throws CommandException {
        if (args.length < min)
            throw new CommandException("Command usage: " + getArgsDescription());
    }
}
