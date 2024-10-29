package pro.gravit.launchserver.launchermodules.remotecontrol.commands;

import pro.gravit.launchserver.launchermodules.remotecontrol.RemoteControlConfig;
import pro.gravit.launchserver.launchermodules.remotecontrol.RemoteControlModule;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.launchserver.utils.helper.LogHelper;

public class ListCommand extends Command {
    public ListCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    public void invoke(String... args) {
        RemoteControlModule module = server.modulesManager.getModule(RemoteControlModule.class);
        for (RemoteControlConfig.RemoteControlToken token : module.config.list) {
            LogHelper.info("Token %s... allow %s commands %s", token.token.substring(0, 5), token.allowAll ? "all" : String.valueOf(token.commands.size()), token.commands.isEmpty() ? "" : String.join(", ", token.commands));
        }
        LogHelper.info("Found %d tokens", module.config.list.size());
    }
}
