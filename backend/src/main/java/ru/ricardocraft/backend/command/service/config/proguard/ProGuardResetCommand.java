package ru.ricardocraft.backend.command.service.config.proguard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.components.ProGuardComponent;

import java.nio.file.Files;

@Component
@RequiredArgsConstructor
public class ProGuardResetCommand extends Command {

    private final ProGuardComponent proGuardComponent;

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
        proGuardComponent.getProguardConf().prepare(true);
        Files.deleteIfExists(proGuardComponent.getProguardConf().mappings);
    }
}
