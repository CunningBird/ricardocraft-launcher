package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

@Component
public class CurseforgeCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();
    private final CurseforgeAPI api;

    @Autowired
    public CurseforgeCommand(LaunchServerConfig config) {
        super();
        this.api = new CurseforgeAPI(config.mirrorConfig.curseforgeApiKey);
        this.childCommands.put("getMod", new SubCommand("[modId]", "Get mod info by id") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                var e = api.fetchModById(Long.parseLong(args[0]));
                logger.info("Response: {}", Launcher.gsonManager.configGson.toJson(e));
            }
        });
        this.childCommands.put("getModFile", new SubCommand("[modId] [fileId]", "Get mod file info by id") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 2);
                var e = api.fetchModFileById(Long.parseLong(args[0]), Long.parseLong(args[1]));
                logger.info("Response: {}", Launcher.gsonManager.configGson.toJson(e));
            }
        });
    }

    @Override
    public String getArgsDescription() {
        return "[action] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "access curseforge api";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
