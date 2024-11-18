package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.GsonManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.mirror.MirrorWorkspace;

import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ApplyWorkspaceCommand extends Command {
    private final Logger logger = LogManager.getLogger(ApplyWorkspaceCommand.class);

    private transient final MirrorManager mirrorManager;
    private transient final GsonManager gsonManager;

    @Autowired
    public ApplyWorkspaceCommand(MirrorManager mirrorManager, GsonManager gsonManager) {
        super();
        this.mirrorManager = mirrorManager;
        this.gsonManager = gsonManager;
    }

    @Override
    public String getArgsDescription() {
        return "[path]";
    }

    @Override
    public String getUsageDescription() {
        return "apply workspace. This action remove your files in workspace!";
    }

    @Override
    public void invoke(String... args) throws Exception {
        URI url = null;
        Path workspaceFilePath = null;
        if(args.length == 0) {
            url = mirrorManager.getDefaultMirror().getURL("workspace.json").toURI();
        } else if(args[0].startsWith("http://") || args[0].startsWith("https://")) {
            url = new URI(args[0]);
        } else {
            workspaceFilePath = Paths.get(args[0]);
        }
        if(url != null) {
            workspaceFilePath = mirrorManager.getTools().getConfigDir().resolve("workspace.json");
            logger.info("Download {} to {}", url, workspaceFilePath);
            Downloader.downloadFile(url, workspaceFilePath, null).getFuture().get();
        }
        MirrorWorkspace workspace;
        try(Reader reader = IOHelper.newReader(workspaceFilePath)) {
            workspace = gsonManager.gson.fromJson(reader, MirrorWorkspace.class);
        }
        Path workspacePath = mirrorManager.getTools().getWorkspaceDir();
        if(Files.exists(workspacePath)) {
            logger.warn("THIS ACTION DELETE ALL FILES IN {}", workspacePath);
            IOHelper.deleteDir(workspacePath, false);
        } else {
            Files.createDirectories(workspacePath);
        }
        mirrorManager.getTools().applyWorkspace(workspace, workspaceFilePath);
        logger.info("Complete");
    }
}
