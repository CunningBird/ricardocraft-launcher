package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.command.Command;

@Component
@RequiredArgsConstructor
public class AuthLimiterClearCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(AuthLimiterClearCommand.class);

    private final AuthLimiter authLimiter;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Clear authLimiter map";
    }

    @Override
    public void invoke(String... args) throws Exception {
        long size = authLimiter.getMap().size();
        authLimiter.getMap().clear();
        logger.info("Cleared {} entity", size);
    }
}
