package ru.ricardocraft.backend.command.mirror;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.helper.IOHelper;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorkspaceCommand extends Command {

    public WorkspaceCommand(LaunchServer server) {
        super(server);
        SubCommand clearClient = new SubCommand("[vanilla/forge/fabric/neoforge] [version]", "remove client cache with specific loader and version") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 2);
                Path target = server.mirrorManager.getTools().getWorkspaceDir().resolve("clients").resolve(args[0]);
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
