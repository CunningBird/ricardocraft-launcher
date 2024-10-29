package pro.gravit.launchserver.command.mirror;

import pro.gravit.launchserver.command.mirror.installers.FabricInstallerCommand;
import pro.gravit.launchserver.command.mirror.installers.ForgeInstallerCommand;
import pro.gravit.launchserver.command.mirror.installers.QuiltInstallerCommand;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;

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
