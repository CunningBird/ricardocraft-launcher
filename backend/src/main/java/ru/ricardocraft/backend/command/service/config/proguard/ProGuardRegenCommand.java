package ru.ricardocraft.backend.command.service.config.proguard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.config.ProguardConfig;

@Component
@RequiredArgsConstructor
public class ProGuardRegenCommand extends Command {

    private final ProguardConfig proguardConfig;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "regenerate proguard dictionary";
    }

    @Override
    public void invoke(String... args) throws Exception {
        proguardConfig.genWords(true);
    }
}
