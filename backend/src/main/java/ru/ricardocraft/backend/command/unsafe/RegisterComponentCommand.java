package ru.ricardocraft.backend.command.unsafe;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.components.Component;

@org.springframework.stereotype.Component
@NoArgsConstructor
public class RegisterComponentCommand extends Command {

    @Override
    public String getArgsDescription() {
        return "[name] [classname]";
    }

    @Override
    public String getUsageDescription() {
        return "register custom component";
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        Class clazz = Class.forName(args[1]);
        Component.providers.register(args[0], clazz);
    }
}
