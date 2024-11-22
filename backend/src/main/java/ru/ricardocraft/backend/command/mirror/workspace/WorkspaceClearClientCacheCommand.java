package ru.ricardocraft.backend.command.mirror.workspace;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.MirrorManager;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class WorkspaceClearClientCacheCommand extends Command {

    private final DirectoriesManager directoriesManager;

    public WorkspaceClearClientCacheCommand(DirectoriesManager directoriesManager) {
        this.directoriesManager = directoriesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[vanilla/forge/fabric/neoforge] [version]";
    }

    @Override
    public String getUsageDescription() {
        return "remove client cache with specific loader and version";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        Path target = directoriesManager.getMirrorHelperWorkspaceDir().resolve("clients").resolve(args[0]);
        if (!Files.isDirectory(target)) {
            throw new FileNotFoundException(target.toString());
        }
        IOHelper.deleteDir(target, true);
    }
}
