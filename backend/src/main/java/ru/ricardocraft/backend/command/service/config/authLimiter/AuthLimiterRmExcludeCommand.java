package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.components.AuthLimiterComponent;

@Component
@RequiredArgsConstructor
public class AuthLimiterRmExcludeCommand extends Command {

    private final AuthLimiterComponent authLimiterComponent;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Remove exclusion from authLimiter";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        authLimiterComponent.exclude.remove(authLimiterComponent.getFromString(args[0]));
    }
}
