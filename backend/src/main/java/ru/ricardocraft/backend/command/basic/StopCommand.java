package ru.ricardocraft.backend.command.basic;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.JVMHelper;

public final class StopCommand extends Command {
    public StopCommand(LaunchServer server) {
        super(server);
    }

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
