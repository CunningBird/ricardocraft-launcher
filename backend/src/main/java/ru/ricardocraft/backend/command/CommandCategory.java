package ru.ricardocraft.backend.command;

import java.util.Map;

public interface CommandCategory {
    void registerCommand(String name, Command command);

    Command findCommand(String name);

    Map<String, Command> commandsMap();
}
