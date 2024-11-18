package ru.ricardocraft.backend.command.mirror.token;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.AuthManager;

@Component
public class TokenServerCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(TokenServerCommand.class);

    private final AuthManager authManager;
    private final ProfileProvider profileProvider;
    private final AuthProviders authProviders;

    @Autowired
    public TokenServerCommand(AuthManager authManager,
                              ProfileProvider profileProvider,
                              AuthProviders authProviders) {
        this.authManager = authManager;
        this.profileProvider = profileProvider;
        this.authProviders = authProviders;
    }

    @Override
    public String getArgsDescription() {
        return "[profileName] (authId) (public only)";
    }

    @Override
    public String getUsageDescription() {
        return "generate new server token";
    }

    @Override
    public void invoke(String... args) throws Exception {
        AuthProviderPair pair = args.length > 1 ? authProviders.getAuthProviderPair(args[1]) : authProviders.getAuthProviderPair();
        boolean publicOnly = args.length <= 2 || Boolean.parseBoolean(args[2]);
        ClientProfile profile = null;
        for (ClientProfile p : profileProvider.getProfiles()) {
            if (p.getTitle().equals(args[0]) || p.getUUID().toString().equals(args[0])) {
                profile = p;
                break;
            }
        }
        if (profile == null) {
            logger.warn("Profile {} not found", args[0]);
        }
        if (pair == null) {
            logger.error("AuthId {} not found", args[1]);
            return;
        }
        String token = authManager.newCheckServerToken(profile != null ? profile.getUUID().toString() : args[0], pair.name, publicOnly);
        logger.info("Server token {} authId {}: {}", args[0], pair.name, token);
    }
}
