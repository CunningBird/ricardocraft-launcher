package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

@Component
public class ListProfilesCommand extends Command {

    private final transient Logger logger = LogManager.getLogger(ListProfilesCommand.class);

    private transient final LaunchServerConfig config;

    @Autowired
    public ListProfilesCommand(LaunchServerConfig server) {
        super();
        this.config = server;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "show all profiles";
    }

    @Override
    public void invoke(String... args) throws Exception {
        for(var profile : config.profileProvider.getProfiles()) {
            logger.info("{} ({}) {}", profile.getTitle(), profile.getVersion().toString(), profile.isLimited() ? "limited" : "");
        }
    }
}
