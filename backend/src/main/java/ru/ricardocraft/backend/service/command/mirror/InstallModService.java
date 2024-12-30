package ru.ricardocraft.backend.service.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.mirror.InstallClient;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstallModService {

    private final InstallClient installClient;
    private final DirectoriesService directoriesService;

    public void installMods(String modDir, String modVersion, String loaderName, String[] modsList) throws Exception {
        Path dir = directoriesService.getUpdatesDir().resolve(modDir);
        if (Files.notExists(dir)) {
            throw new FileNotFoundException(dir.toString());
        }
        Version version = parseClientVersion(modVersion);
        Path modsDir = dir.resolve("mods");
        List<String> mods = List.of(modsList);
        if (!mods.isEmpty()) {
            for (var modId : mods) {
                try {
                    try {
                        long id = Long.parseLong(modId);
                        installClient.installMod(modsDir, id, version);
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                    installClient.installMod(modsDir, modId, loaderName, version);
                } catch (Throwable e) {
                    log.warn("Mod {} not installed! Exception {}", modId, e.getMessage());
                }
            }
            log.info("Mods installed");
        }
    }

    protected Version parseClientVersion(String arg) throws Exception {
        if (arg.isEmpty()) throw new Exception("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
