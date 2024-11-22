package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.command.Command;

@Component
@RequiredArgsConstructor
public class AuthLimiterGcCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(AuthLimiterGcCommand.class);

    private final AuthLimiter authLimiter;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "invoke GC for authLimiter";
    }

    @Override
    public void invoke(String... args) throws Exception {
        long size = authLimiter.getMap().size();
        authLimiter.garbageCollection();
        logger.info("Cleared {} entity", size);
    }
}
