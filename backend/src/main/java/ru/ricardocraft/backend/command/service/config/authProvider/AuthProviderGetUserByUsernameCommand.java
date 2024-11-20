package ru.ricardocraft.backend.command.service.config.authProvider;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.repository.User;

@Component
@RequiredArgsConstructor
public class AuthProviderGetUserByUsernameCommand extends Command {

    private static final Logger logger = LogManager.getLogger(AuthProviderGetUserByUsernameCommand.class);

    private final AuthCoreProvider authCoreProvider;

    @Override
    public String getArgsDescription() {
        return "[username]";
    }

    @Override
    public String getUsageDescription() {
        return "get user by username";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        User user = authCoreProvider.getUserByUsername(args[0]);
        if (user == null) {
            logger.info("User {} not found", args[0]);
        } else {
            logger.info("User {}: {}", args[0], user.toString());
        }
    }
}
