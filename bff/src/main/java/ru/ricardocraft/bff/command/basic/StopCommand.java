package ru.ricardocraft.bff.command.basic;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.helper.JVMHelper;

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
