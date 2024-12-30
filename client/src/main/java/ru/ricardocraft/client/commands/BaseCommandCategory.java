package ru.ricardocraft.client.commands;

import org.springframework.stereotype.Component;
import ru.ricardocraft.client.helper.VerifyHelper;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BaseCommandCategory {
    private final Map<String, Command> commands = new ConcurrentHashMap<>(32);

    public void registerCommand(String name, Command command) {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(commands, name.toLowerCase(), Objects.requireNonNull(command, "command"),
                String.format("Command has been already registered: '%s'", name.toLowerCase()));
    }

    public void unregisterCommand(String name) {
        commands.remove(name);
    }

    public Command findCommand(String name) {
        return commands.get(name);
    }

    public Map<String, Command> commandsMap() {
        return commands;
    }
}
