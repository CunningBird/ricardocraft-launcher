package ru.ricardocraft.backend.command.mirror;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class WorkspaceClearClientCacheCommand {

    private final DirectoriesService directoriesService;

    @ShellMethod("[vanilla/forge/fabric/neoforge] remove client cache with specific loader and version")
    public void workspaceClearClientCache(@ShellOption String engine) throws Exception {
        Path target = directoriesService.getMirrorHelperWorkspaceDir().resolve("clients").resolve(engine);
        if (!Files.isDirectory(target)) {
            throw new FileNotFoundException(target.toString());
        }
        IOHelper.deleteDir(target, true);
    }
}