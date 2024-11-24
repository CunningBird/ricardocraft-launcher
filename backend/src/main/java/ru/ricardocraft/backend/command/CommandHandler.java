package ru.ricardocraft.backend.command;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Getter
public abstract class CommandHandler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final List<Category> categories = new ArrayList<>();
    private final CommandCategory baseCategory = new BaseCommandCategory();

    public void eval(String line, boolean bell) {
        logger.info("Command '{}'", line);
        try {
            evalNative(line, bell);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void evalNative(String line, boolean bell) throws Exception {
        String[] args;
        args = parseCommand(line);
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

    @Override
    public void run() {
        try {
            readLoop();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Walk all categories
     * Categories are sorted in the order they are added.
     * The base category is walked last
     *
     * @param callback your callback
     */
    public void walk(CommandWalk callback) {
        for (Category category : getCategories()) {
            for (Map.Entry<String, Command> entry : category.category.commandsMap().entrySet())
                callback.walk(category, entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Command> entry : getBaseCategory().commandsMap().entrySet())
            callback.walk(null, entry.getKey(), entry.getValue());
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

    private String[] parseCommand(CharSequence line) throws CommandException {
        boolean quoted = false;
        boolean wasQuoted = false;

        // Read line char by char
        Collection<String> result = new LinkedList<>();
        StringBuilder builder = new StringBuilder(100);
        for (int i = 0; i <= line.length(); i++) {
            boolean end = i >= line.length();
            char ch = end ? '\0' : line.charAt(i);

            // Maybe we should read next argument?
            if (end || !quoted && Character.isWhitespace(ch)) {
                if (end && quoted)
                    throw new CommandException("Quotes wasn't closed");

                // Empty args are ignored (except if was quoted)
                if (wasQuoted || !builder.isEmpty())
                    result.add(builder.toString());

                // Reset file builder
                wasQuoted = false;
                builder.setLength(0);
                continue;
            }

            // Append next char
            switch (ch) {
                case '"': // "abc"de, "abc""de" also allowed
                    quoted = !quoted;
                    wasQuoted = true;
                    break;
                case '\\': // All escapes, including spaces etc
                    if (i + 1 >= line.length())
                        throw new CommandException("Escape character is not specified");
                    char next = line.charAt(i + 1);
                    builder.append(next);
                    i++;
                    break;
                default: // Default char, simply append
                    builder.append(ch);
                    break;
            }
        }

        // Return result as array
        return result.toArray(new String[0]);
    }

    @FunctionalInterface
    public interface CommandWalk {
        void walk(Category category, String name, Command command);
    }

    public static class Category {
        public final CommandCategory category;
        public final String name;
        public String description;

        public Category(CommandCategory category, String name) {
            this.category = category;
            this.name = name;
        }

        public Category(CommandCategory category, String name, String description) {
            this.category = category;
            this.name = name;
            this.description = description;
        }
    }
}
