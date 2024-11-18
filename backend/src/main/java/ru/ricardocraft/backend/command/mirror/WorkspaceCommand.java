package ru.ricardocraft.backend.command.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.manangers.MirrorManager;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class WorkspaceCommand extends Command {

    @Autowired
    public WorkspaceCommand(MirrorManager mirrorManager) {
        super();
        SubCommand clearClient = new SubCommand("[vanilla/forge/fabric/neoforge] [version]", "remove client cache with specific loader and version") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 2);
                Path target = mirrorManager.getTools().getWorkspaceDir().resolve("clients").resolve(args[0]);
                if(!Files.isDirectory(target)) {
                    throw new FileNotFoundException(target.toString());
                }
                IOHelper.deleteDir(target, true);
            }
        };
        childCommands.put("clearclientcache", clearClient);
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
