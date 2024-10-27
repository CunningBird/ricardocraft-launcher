package pro.gravit.launcher.gui.utils.command;

import pro.gravit.launcher.gui.utils.command.Command;

import java.util.Map;

public interface CommandCategory {
    void registerCommand(String name, Command command);

    Command unregisterCommand(String name);

    Command findCommand(String name);

    Map<String, Command> commandsMap();
}
