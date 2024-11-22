package ru.ricardocraft.backend.command.remotecontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.RemoteControlTokenProperties;

public class ListCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(ListCommand.class);

    private final transient LaunchServerProperties config;

    public ListCommand(LaunchServerProperties config) {
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

        for (RemoteControlTokenProperties token : config.getRemoteControl().getList()) {
            logger.info("Token {}... allow {} commands {}", token.getToken().substring(0, 5), token.getAllowAll()
                    ? "all" : String.valueOf(token.getCommands().size()), token.getCommands().isEmpty() ? "" : String.join(", ", token.getCommands()));
        }
        logger.info("Found {} tokens", config.getRemoteControl().getList().size());
    }
}
