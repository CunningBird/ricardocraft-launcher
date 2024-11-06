package ru.ricardocraft.bff.command.remotecontrol;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.config.LaunchServerConfig;
import ru.ricardocraft.bff.helper.LogHelper;

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

        for (LaunchServerConfig.RemoteControlConfig.RemoteControlToken token : server.config.remoteControlConfig.list) {
            LogHelper.info("Token %s... allow %s commands %s", token.token.substring(0, 5), token.allowAll ? "all" : String.valueOf(token.commands.size()), token.commands.isEmpty() ? "" : String.join(", ", token.commands));
        }
        LogHelper.info("Found %d tokens", server.config.remoteControlConfig.list.size());
    }
}
