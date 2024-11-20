package ru.ricardocraft.backend.command.mirror;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.mirror.InstallClient;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.manangers.mirror.modapi.ModrinthAPI;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Component
public class InstallModCommand extends Command {

    private static final Logger logger = LogManager.getLogger(InstallModCommand.class);

    private final LaunchServerDirectories directories;
    private final ModrinthAPI modrinthAPI;
    private final CurseforgeAPI curseforgeApi;
    private final JacksonManager jacksonManager;

    @Autowired
    public InstallModCommand(LaunchServerDirectories directories,
                             CurseforgeAPI curseforgeAPI,
                             ModrinthAPI modrinthAPI,
                             JacksonManager jacksonManager) {
        super();
        this.directories = directories;
        this.curseforgeApi = curseforgeAPI;
        this.modrinthAPI = modrinthAPI;
        this.jacksonManager = jacksonManager;
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
        Path modsDir = dir.resolve("mods");
        String loaderName = args[2];
        List<String> mods = Arrays.stream(args[3].split(",")).toList();
        if (!mods.isEmpty()) {
            for (var modId : mods) {
                try {
                    try {
                        long id = Long.parseLong(modId);
                        InstallClient.installMod(curseforgeApi, modsDir, id, version);
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                    InstallClient.installMod(modrinthAPI, modsDir, modId, loaderName, version);
                } catch (Throwable e) {
                    logger.warn("Mod {} not installed! Exception {}", modId, e);
                }
            }
            logger.info("Mods installed");
        }
    }

    protected ClientProfile.Version parseClientVersion(String arg) throws CommandException, JsonProcessingException {
        if(arg.isEmpty()) {
            throw new CommandException("ClientVersion can't be empty");
        }
        return jacksonManager.getMapper().readValue(arg, ClientProfile.Version.class);
    }
}
