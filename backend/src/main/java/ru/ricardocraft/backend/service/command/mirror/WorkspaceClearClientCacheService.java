package ru.ricardocraft.backend.service.command.mirror;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class WorkspaceClearClientCacheService {

    private final DirectoriesService directoriesService;

    public void workspaceClearClientCache(String engine) throws Exception {
        Path target = directoriesService.getMirrorHelperWorkspaceDir().resolve("clients").resolve(engine);
        if (!Files.isDirectory(target)) {
            throw new FileNotFoundException(target.toString());
        }
        IOHelper.deleteDir(target, true);
    }
}