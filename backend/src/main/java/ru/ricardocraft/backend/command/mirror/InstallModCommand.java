package ru.ricardocraft.backend.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.mirror.InstallClient;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class InstallModCommand {

    private final InstallClient installClient;
    private final DirectoriesManager directoriesManager;

    @ShellMethod("[dir] [version] [forge/fabric] [mod1,mod2,mod3]")
    public void installMods(@ShellOption String modDir,
                            @ShellOption String modVersion,
                            @ShellOption String loaderName,
                            @ShellOption String[] modsList) throws Exception {
        Path dir = directoriesManager.getUpdatesDir().resolve(modDir);
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

    protected Version parseClientVersion(String arg) throws CommandException {
        if (arg.isEmpty()) throw new CommandException("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
