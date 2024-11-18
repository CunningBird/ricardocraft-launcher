package ru.ricardocraft.backend.command.service.config.proguard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.ProGuard;
import ru.ricardocraft.backend.command.Command;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class ProGuardCleanCommand extends Command {

    private final ProGuard proGuard;

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
        proGuard.getProguardConf().prepare(true);
        Files.deleteIfExists(proGuard.getProguardConf().mappings);
    }
}
