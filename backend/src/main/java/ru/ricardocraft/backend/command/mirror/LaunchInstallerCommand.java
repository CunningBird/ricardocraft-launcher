package ru.ricardocraft.backend.command.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.mirror.installers.FabricInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.ForgeInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.QuiltInstallerCommand;

@Component
public class LaunchInstallerCommand extends Command {

    @Autowired
    public LaunchInstallerCommand(FabricInstallerCommand fabricInstallerCommand,
                                  ForgeInstallerCommand forgeInstallerCommand,
                                  QuiltInstallerCommand quiltInstallerCommand) {
        super();
        childCommands.put("fabric", fabricInstallerCommand);
        childCommands.put("forge", forgeInstallerCommand);
        childCommands.put("quilt", quiltInstallerCommand);
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
