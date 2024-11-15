package ru.ricardocraft.backend.command.remotecontrol;

import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.LogHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

public class ListCommand extends Command {

    private final transient LaunchServerConfig config;

    public ListCommand(LaunchServerConfig config) {
        super();
        this.config = config;
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

        for (LaunchServerConfig.RemoteControlConfig.RemoteControlToken token : config.remoteControlConfig.list) {
            LogHelper.info("Token %s... allow %s commands %s", token.token.substring(0, 5), token.allowAll ? "all" : String.valueOf(token.commands.size()), token.commands.isEmpty() ? "" : String.join(", ", token.commands));
        }
        LogHelper.info("Found %d tokens", config.remoteControlConfig.list.size());
    }
}
