package ru.ricardocraft.backend.command.remotecontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

public class ListCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(ListCommand.class);

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
            logger.info("Token {}... allow {} commands {}", token.token.substring(0, 5), token.allowAll ? "all" : String.valueOf(token.commands.size()), token.commands.isEmpty() ? "" : String.join(", ", token.commands));
        }
        logger.info("Found {} tokens", config.remoteControlConfig.list.size());
    }
}
