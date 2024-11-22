package ru.ricardocraft.backend.command.service.config.proguard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.config.ProguardConfig;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class ProGuardCleanCommand extends Command {

    private final ProguardConfig proguardConfig;

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "reset proguard config";
    }

    @Override
    public void invoke(String... args) throws Exception {
        proguardConfig.prepare(true);
        Files.deleteIfExists(proguardConfig.mappings);
    }
}
