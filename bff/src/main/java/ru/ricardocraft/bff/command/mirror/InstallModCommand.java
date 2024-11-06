package ru.ricardocraft.bff.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.base.profiles.ClientProfile;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.config.LaunchServerConfig;
import ru.ricardocraft.bff.mirror.InstallClient;
import ru.ricardocraft.bff.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.bff.mirror.modapi.ModrinthAPI;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class InstallModCommand extends Command {

    private static final Logger logger = LogManager.getLogger();
    private final LaunchServerConfig.MirrorConfig config;

    public InstallModCommand(LaunchServer server) {
        super(server);
        this.config = server.config.mirrorConfig;
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
        Path dir = server.updatesDir.resolve(args[0]);
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
