package ru.ricardocraft.client.utils.command;

import ru.ricardocraft.client.utils.helper.CommonHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandHandler implements Runnable {

    private final List<Category> categories = new ArrayList<>();
    private final BaseCommandCategory baseCategory = new BaseCommandCategory();

    public void eval(String line, boolean bell) {
        LogHelper.info("Command '%s'", line);
        try {
            evalNative(line, bell);
        } catch (Exception e) {
            LogHelper.error(e);
        }
    }

    public void evalNative(String line, boolean bell) throws Exception {
        String[] args;
        args = CommonHelper.parseCommand(line);
        if (args.length > 0) args[0] = args[0].toLowerCase();
        eval(args, bell);
    }

    public void eval(String[] args, boolean bell) throws Exception {
        if (args.length == 0)
            return;

        // Measure start time and invoke command
        long startTime = System.currentTimeMillis();
        lookup(args[0]).invoke(Arrays.copyOfRange(args, 1, args.length));

        // Bell if invocation took > 1s
        long endTime = System.currentTimeMillis();
        if (bell && endTime - startTime >= 5000)
            bell();
    }

    public Command lookup(String name) throws CommandException {
        Command command = findCommand(name);
        if (command == null)
            throw new CommandException(String.format("Unknown command: '%s'", name));
        return command;
    }

    public Command findCommand(String name) {
        Command cmd = baseCategory.findCommand(name);
        if (cmd == null) {
            for (Category entry : categories) {
                cmd = entry.category.findCommand(name);
                if (cmd != null) return cmd;
            }
        }
        return cmd;
    }

    /**
     * Reads a line from the console
     *
     * @return command line
     * @throws IOException Internal Error
     */
    public abstract String readLine() throws IOException;

    private void readLoop() throws IOException {
        for (String line = readLine(); line != null; line = readLine())
            eval(line, true);
    }

    public void registerCommand(String name, Command command) {
        baseCategory.registerCommand(name, command);
    }

    public void registerCategory(Category category) {
        categories.add(category);
    }

    public void unregisterCommand(String name) {
        baseCategory.unregisterCommand(name);
    }

    @Override
    public void run() {
        try {
            readLoop();
        } catch (IOException e) {
            LogHelper.error(e);
        }
    }

    public BaseCommandCategory getBaseCategory() {
        return baseCategory;
    }

    public List<Category> getCategories() {
        return categories;
    }

    /**
     * If supported, sends a bell signal to the console
     */
    public abstract void bell();

    /**
     * Cleans the console
     *
     * @throws IOException Internal Error
     */
    public abstract void clear() throws IOException;
}
