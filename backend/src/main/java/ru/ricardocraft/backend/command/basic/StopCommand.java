package ru.ricardocraft.backend.command.basic;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.JVMHelper;

@Component
public final class StopCommand extends Command {

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Stop LaunchServer";
    }

    @Override
    public void invoke(String... args) {
        JVMHelper.RUNTIME.exit(0);
    }
}