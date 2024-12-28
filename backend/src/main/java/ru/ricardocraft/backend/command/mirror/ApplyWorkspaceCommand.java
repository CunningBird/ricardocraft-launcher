package ru.ricardocraft.backend.command.mirror;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.properties.config.MirrorWorkspaceProperties;

import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class ApplyWorkspaceCommand {

    private final DirectoriesManager directoriesManager;
    private final MirrorManager mirrorManager;
    private final JacksonManager jacksonManager;
    private final LwjglDownloadCommand lwjglDownloadCommand;

    @ShellMethod("[path] apply workspace. This action remove your files in workspace!")
    public void applyWorkspace(@ShellOption(defaultValue = ShellOption.NULL) String path) throws Exception {
        URI url = null;
        Path workspaceFilePath = null;
        if (path == null) {
            url = mirrorManager.getDefaultMirror().getURL("workspace.json").toURI();
        } else if (path.startsWith("http://") || path.startsWith("https://")) {
            url = new URI(path);
        } else {
            workspaceFilePath = Paths.get(path);
        }
        if (url != null) {
            workspaceFilePath = directoriesManager.getMirrorHelperDir().resolve("workspace.json");
            log.info("Download {} to {}", url, workspaceFilePath);
            Downloader.downloadFile(url, workspaceFilePath, null).getFuture().get();
        }
        MirrorWorkspaceProperties workspace;
        try (Reader reader = IOHelper.newReader(workspaceFilePath)) {
            workspace = jacksonManager.getMapper().readValue(reader, MirrorWorkspaceProperties.class);
        }
        Path workspacePath = directoriesManager.getMirrorHelperWorkspaceDir();
        if (Files.exists(workspacePath)) {
            log.warn("THIS ACTION DELETE ALL FILES IN {}", workspacePath);
            IOHelper.deleteDir(workspacePath, false);
        } else {
            Files.createDirectories(workspacePath);
        }
        applyWorkspace(workspace);
        log.info("Complete");
    }

    public void applyWorkspace(MirrorWorkspaceProperties workspace) throws Exception {
        Path workspacePath = directoriesManager.getMirrorHelperWorkspaceDir();
        Path tmp = Files.createTempDirectory("mirrorhelper");
        try {
            log.info("Apply workspace");
            log.info("Download libraries");
            for (var l : workspace.getLibraries()) {
                if (l.getData() != null) {
                    IOHelper.createParentDirs(workspacePath.resolve(l.getPath()));
                    IOHelper.write(workspacePath.resolve(l.getPath()), l.getData().getBytes(StandardCharsets.UTF_8));
                    continue;
                }
                String randomName = SecurityHelper.randomStringAESKey();
                Path tmpPath = tmp.resolve(randomName);
                log.info("Download {} to {}", l.getUrl(), tmpPath);
                Downloader.downloadFile(new URI(l.getUrl()), tmpPath, null).getFuture().get();
                if (l.getPath() != null) {
                    Path lPath = workspacePath.resolve(l.getPath());
                    IOHelper.createParentDirs(lPath);
                    if (l.getPrefixFilter() != null) {
                        Predicate<String> pred = (p) -> {
                            for (var e : l.getPrefixFilter()) {
                                if (p.startsWith(e)) {
                                    return true;
                                }
                            }
                            return false;
                        };
                        try (ZipInputStream input = IOHelper.newZipInput(tmpPath)) {
                            try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(lPath))) {
                                ZipEntry e = input.getNextEntry();
                                while (e != null) {
                                    if (pred.test(e.getName())) {
                                        ZipEntry entry = new ZipEntry(e.getName());
                                        output.putNextEntry(entry);
                                        input.transferTo(output);
                                    }
                                    e = input.getNextEntry();
                                }
                            }
                        }
                    } else {
                        IOHelper.copy(tmpPath, lPath);
                    }
                }
                if (l.getUnpack() != null) {
                    try (ZipInputStream input = IOHelper.newZipInput(tmpPath)) {
                        ZipEntry e = input.getNextEntry();
                        while (e != null) {
                            String target = l.getUnpack().get(e.getName());
                            if (target != null) {
                                Path targetPath = workspacePath.resolve(target);
                                IOHelper.createParentDirs(targetPath);
                                try (OutputStream output = IOHelper.newOutput(targetPath)) {
                                    input.transferTo(output);
                                }
                            }
                            e = input.getNextEntry();
                        }
                    }
                }
            }
            log.info("Download multiMods");
            for (var e : workspace.getMultiMods().entrySet()) {
                Path target = workspacePath.resolve("multimods").resolve(e.getKey().concat(".jar"));
                log.info("Download {} to {}", e.getValue().getUrl(), target);
                Downloader.downloadFile(new URI(e.getValue().getUrl()), target, null).getFuture().get();
            }
            log.info("Install lwjgl3 directory");
            lwjglDownloadCommand.lwjglDownload(workspace.getLwjgl3version(), "mirrorhelper-tmp-lwjgl3");
            Path lwjgl3Path = workspacePath.resolve("workdir").resolve("lwjgl3");
            IOHelper.move(directoriesManager.getUpdatesDir().resolve("mirrorhelper-tmp-lwjgl3"), lwjgl3Path);
            Files.deleteIfExists(directoriesManager.getUpdatesDir().resolve("mirrorhelper-tmp-lwjgl3"));
            log.info("Save config");
        } finally {
            IOHelper.deleteDir(tmp, true);
        }
    }
}
