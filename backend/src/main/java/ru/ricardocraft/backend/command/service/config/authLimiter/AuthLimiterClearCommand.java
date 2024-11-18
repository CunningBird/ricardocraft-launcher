package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.components.AuthLimiterComponent;

@Component
@RequiredArgsConstructor
public class AuthLimiterClearCommand extends Command {

    private transient final Logger logger = LogManager.getLogger();

    private final AuthLimiterComponent authLimiterComponent;

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
        long size = authLimiterComponent.getMap().size();
        authLimiterComponent.getMap().clear();
        logger.info("Cleared {} entity", size);
    }
}
