package ru.ricardocraft.backend.command.mirror.curseforge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;

@Component
public class CurseforgeGetModFileCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(CurseforgeGetModFileCommand.class);

    private final CurseforgeAPI api;
    private final JacksonManager jacksonManager;

    @Autowired
    public CurseforgeGetModFileCommand(CurseforgeAPI api, JacksonManager gsonManager) {
        this.api = api;
        this.jacksonManager = gsonManager;
    }

    @Override
    public String getArgsDescription() {
        return "[modId] [fileId]";
    }

    @Override
    public String getUsageDescription() {
        return "Get mod file info by id";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        var e = api.fetchModFileById(Long.parseLong(args[0]), Long.parseLong(args[1]));
        logger.info("Response: {}", jacksonManager.getMapper().writeValueAsString(e));
    }
}
