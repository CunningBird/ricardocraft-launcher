package ru.ricardocraft.backend.command.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.mirror.workspace.WorkspaceClearClientCacheCommand;

@Component
public class WorkspaceCommand extends Command {

    @Autowired
    public WorkspaceCommand(WorkspaceClearClientCacheCommand workspaceClearClientCacheCommand) {
        super();
        childCommands.put("clearclientcache", workspaceClearClientCacheCommand);
    }

    @Override
    public String getArgsDescription() {
        return "[command]";
    }

    @Override
    public String getUsageDescription() {
        return "workspace tools";
    }

    @Override
    public void invoke(String... strings) throws Exception {
        invokeSubcommands(strings);
    }
}
