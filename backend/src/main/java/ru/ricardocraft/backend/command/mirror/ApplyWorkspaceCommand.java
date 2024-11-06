package ru.ricardocraft.backend.command.mirror;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.mirror.MirrorWorkspace;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.MirrorConfig;

import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplyWorkspaceCommand extends Command {
    private final MirrorConfig config;
    private final Logger logger = LogManager.getLogger(ApplyWorkspaceCommand.class);

    public ApplyWorkspaceCommand(LaunchServer server) {
        super(server);
        this.config = server.config.mirrorConfig;
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
            url = server.mirrorManager.getDefaultMirror().getURL("workspace.json").toURI();
        } else if(args[0].startsWith("http://") || args[0].startsWith("https://")) {
            url = new URI(args[0]);
        } else {
            workspaceFilePath = Paths.get(args[0]);
        }
        if(url != null) {
            workspaceFilePath = server.mirrorManager.getTools().getConfigDir().resolve("workspace.json");
            logger.info("Download {} to {}", url, workspaceFilePath);
            Downloader.downloadFile(url, workspaceFilePath, null).getFuture().get();
        }
        MirrorWorkspace workspace;
        try(Reader reader = IOHelper.newReader(workspaceFilePath)) {
            workspace = Launcher.gsonManager.gson.fromJson(reader, MirrorWorkspace.class);
        }
        Path workspacePath = server.mirrorManager.getTools().getWorkspaceDir();
        if(Files.exists(workspacePath)) {
            logger.warn("THIS ACTION DELETE ALL FILES IN {}", workspacePath);
            if(!showApplyDialog("Continue?")) {
                return;
            }
            IOHelper.deleteDir(workspacePath, false);
        } else {
            Files.createDirectories(workspacePath);
        }
        server.mirrorManager.getTools().applyWorkspace(workspace, workspaceFilePath);
        logger.info("Complete");
    }
}
