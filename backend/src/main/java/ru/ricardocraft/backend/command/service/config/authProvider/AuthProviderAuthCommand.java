package ru.ricardocraft.backend.command.service.config.authProvider;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.password.AuthPassword;
import ru.ricardocraft.backend.auth.password.AuthPlainPassword;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.JacksonManager;

@Component
@RequiredArgsConstructor
public class AuthProviderAuthCommand extends Command {

    private static final Logger logger = LogManager.getLogger(AuthProviderAuthCommand.class);

    private final AuthCoreProvider authCoreProvider;

    private final JacksonManager jacksonManager;

    @Override
    public String getArgsDescription() {
        return "[login] (json/plain password data)";
    }

    @Override
    public String getUsageDescription() {
        return "Test auth";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        AuthPassword password = null;
        if (args.length > 1) {
            if (args[1].startsWith("{")) {
                password = jacksonManager.getMapper().readValue(args[1], AuthPassword.class);
            } else {
                password = new AuthPlainPassword(args[1]);
            }
        }
        var report = authCoreProvider.authorize(args[0], null, password, false);
        if (report.isUsingOAuth()) {
            logger.info("OAuth: AccessToken: {} RefreshToken: {} MinecraftAccessToken: {}", report.oauthAccessToken(), report.oauthRefreshToken(), report.minecraftAccessToken());
            if (report.session() != null) {
                logger.info("UserSession: id {} expire {} user {}", report.session().getID(), report.session().getExpireIn(), report.session().getUser() == null ? "null" : "found");
                logger.info(report.session().toString());
            }
        } else {
            logger.info("Basic: MinecraftAccessToken: {}", report.minecraftAccessToken());
        }
    }
}
