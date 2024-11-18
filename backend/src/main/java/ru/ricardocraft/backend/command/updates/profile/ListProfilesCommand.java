package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.command.Command;

@Component
public class ListProfilesCommand extends Command {

    private final Logger logger = LogManager.getLogger(ListProfilesCommand.class);

    private transient final ProfileProvider profileProvider;

    @Autowired
    public ListProfilesCommand(ProfileProvider profileProvider) {
        super();
        this.profileProvider = profileProvider;
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
        for (var profile : profileProvider.getProfiles()) {
            logger.info("{} ({}) {}", profile.getTitle(), profile.getVersion().toString(), profile.isLimited() ? "limited" : "");
        }
    }
}
