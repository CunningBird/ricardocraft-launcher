package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.mirror.InstallClient;
import ru.ricardocraft.backend.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.mirror.modapi.ModrinthAPI;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.MirrorConfig;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
public class InstallModCommand extends Command {

    private static final Logger logger = LogManager.getLogger();
    private final MirrorConfig config;
    private final LaunchServerDirectories directories;

    @Autowired
    public InstallModCommand(LaunchServerConfig config, LaunchServerDirectories directories) {
        super();
        this.config = config.mirrorConfig;
        this.directories = directories;
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
        Path dir = directories.updatesDir.resolve(args[0]);
        if (Files.notExists(dir)) {
            throw new FileNotFoundException(dir.toString());
        }
        ClientProfile.Version version = parseClientVersion(args[1]);
        ModrinthAPI modrinthAPI = null;
        CurseforgeAPI curseforgeApi = null;
        Path modsDir = dir.resolve("mods");
        String loaderName = args[2];
        List<String> mods = Arrays.stream(args[3].split(",")).toList();
        if (!mods.isEmpty()) {
            for (var modId : mods) {
                try {
                    try {
                        long id = Long.parseLong(modId);
                        if (curseforgeApi == null) {
                            curseforgeApi = new CurseforgeAPI(config.curseforgeApiKey);
                        }
                        InstallClient.installMod(curseforgeApi, modsDir, id, version);
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                    if (modrinthAPI == null) {
                        modrinthAPI = new ModrinthAPI();
                    }
                    InstallClient.installMod(modrinthAPI, modsDir, modId, loaderName, version);
                } catch (Throwable e) {
                    logger.warn("Mod {} not installed! Exception {}", modId, e);
                }
            }
            logger.info("Mods installed");
        }
    }
}
