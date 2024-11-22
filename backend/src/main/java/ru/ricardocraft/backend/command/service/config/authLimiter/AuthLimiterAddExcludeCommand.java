package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.command.Command;

@Component
@RequiredArgsConstructor
public class AuthLimiterAddExcludeCommand extends Command {

    private final AuthLimiter authLimiter;

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
        authLimiter.exclude.add(authLimiter.getFromString(args[0]));
    }
}
