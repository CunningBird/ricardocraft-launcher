package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.mirror.InstallClient;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
public class InstallModCommand extends Command {

    private static final Logger logger = LogManager.getLogger(InstallModCommand.class);

    private final InstallClient installClient;
    private final DirectoriesManager directoriesManager;

    @Autowired
    public InstallModCommand(InstallClient installClient, DirectoriesManager directoriesManager) {
        super();
        this.installClient = installClient;
        this.directoriesManager = directoriesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[dir] [version] [forge/fabric] [mod1,mod2,mod3]";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 4);
        Path dir = directoriesManager.getUpdatesDir().resolve(args[0]);
        if (Files.notExists(dir)) {
            throw new FileNotFoundException(dir.toString());
        }
        Version version = parseClientVersion(args[1]);
        Path modsDir = dir.resolve("mods");
        String loaderName = args[2];
        List<String> mods = Arrays.stream(args[3].split(",")).toList();
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
                    logger.warn("Mod {} not installed! Exception {}", modId, e);
                }
            }
            logger.info("Mods installed");
        }
    }

    protected Version parseClientVersion(String arg) throws CommandException {
        if (arg.isEmpty()) throw new CommandException("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
