package ru.ricardocraft.backend.command.service.config.authLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.AuthLimiter;
import ru.ricardocraft.backend.command.Command;

@Component
@RequiredArgsConstructor
public class AuthLimiterClearExcludeCommand extends Command {

    private final AuthLimiter authLimiter;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Clear exclusions in authLimiter";
    }

    @Override
    public void invoke(String... args) throws Exception {
        authLimiter.exclude.clear();
    }
}
