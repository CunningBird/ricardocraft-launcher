package ru.ricardocraft.bff.command.mirror;

import ru.ricardocraft.bff.command.mirror.installers.FabricInstallerCommand;
import ru.ricardocraft.bff.command.mirror.installers.ForgeInstallerCommand;
import ru.ricardocraft.bff.command.mirror.installers.QuiltInstallerCommand;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;

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
