package ru.ricardocraft.backend.command.mirror;

import ru.ricardocraft.backend.command.mirror.installers.FabricInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.ForgeInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.QuiltInstallerCommand;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;

public class LaunchInstallerCommand extends Command {
    public LaunchInstallerCommand(LaunchServer server) {
        super(server);
        childCommands.put("fabric", new FabricInstallerCommand(server));
        childCommands.put("forge", new ForgeInstallerCommand(server));
        childCommands.put("quilt", new QuiltInstallerCommand(server));
    }

    @Override
    public String getArgsDescription() {
        return "[installer] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "launch installer";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
