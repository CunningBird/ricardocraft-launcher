package ru.ricardocraft.backend.command.mirror.curseforge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.GsonManager;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;

@Component
public class CurseforgeGetModCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(CurseforgeGetModCommand.class);

    private final CurseforgeAPI api;
    private final GsonManager gsonManager;

    @Autowired
    public CurseforgeGetModCommand(CurseforgeAPI api, GsonManager gsonManager) {
        this.api = api;
        this.gsonManager = gsonManager;
    }

    @Override
    public String getArgsDescription() {
        return "[modId]";
    }

    @Override
    public String getUsageDescription() {
        return "Get mod info by id";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        var e = api.fetchModById(Long.parseLong(args[0]));
        logger.info("Response: {}", gsonManager.configGson.toJson(e));
    }
}
