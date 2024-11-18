package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.base.AuthLimiter;

@Component
@RequiredArgsConstructor
public class AuthLimiterRmExcludeCommand extends Command {

    private final AuthLimiter authLimiter;

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
        authLimiter.exclude.remove(authLimiter.getFromString(args[0]));
    }
}
