package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.components.AuthLimiterComponent;

@Component
@RequiredArgsConstructor
public class AuthLimiterAddExcludeCommand extends Command {

    private final AuthLimiterComponent authLimiterComponent;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Add exclusion to authLimiter";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        authLimiterComponent.exclude.add(authLimiterComponent.getFromString(args[0]));
    }
}
