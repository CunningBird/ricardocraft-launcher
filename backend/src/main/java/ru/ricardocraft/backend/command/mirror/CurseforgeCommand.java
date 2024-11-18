package ru.ricardocraft.backend.command.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.mirror.curseforge.CurseforgeGetModCommand;
import ru.ricardocraft.backend.command.mirror.curseforge.CurseforgeGetModFileCommand;

@Component
public class CurseforgeCommand extends Command {

    @Autowired
    public CurseforgeCommand(CurseforgeGetModCommand curseforgeGetModCommand,
                             CurseforgeGetModFileCommand curseforgeGetModFileCommand) {
        super();
        this.childCommands.put("getMod", curseforgeGetModCommand);
        this.childCommands.put("getModFile", curseforgeGetModFileCommand);
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
