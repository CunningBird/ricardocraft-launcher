package ru.ricardocraft.backend.service.command.mirror;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.properties.config.MirrorWorkspaceProperties;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.MirrorService;

import java.io.File;
import java.io.FileOutputStream;
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
@Component
@RequiredArgsConstructor
public class ApplyWorkspaceService {

    private final RestTemplate restTemplate;
    private final DirectoriesService directoriesService;
    private final MirrorService mirrorService;
    private final ObjectMapper objectMapper;
    private final LwjglDownloadService lwjglDownloadService;

    public void applyWorkspace(@Nullable String path) throws Exception {
        URI url = null;
        Path workspaceFilePath = null;
        if (path == null) {
            url = mirrorService.getDefaultMirror().getURL("workspace.json").toURI();
        } else if (path.startsWith("http://") || path.startsWith("https://")) {
            url = new URI(path);
        } else {
            workspaceFilePath = Paths.get(path);
        }
        if (url != null) {
            workspaceFilePath = directoriesService.getMirrorHelperDir().resolve("workspace.json");
            log.info("Download {} to {}", url, workspaceFilePath);

            File ret = new File(String.valueOf(workspaceFilePath));
            restTemplate.execute(url, HttpMethod.GET, null, clientHttpResponse -> {
                FileOutputStream fileIO = new FileOutputStream(ret);
                StreamUtils.copy(clientHttpResponse.getBody(), fileIO);
                fileIO.close();
                return ret;
            });
        }
        MirrorWorkspaceProperties workspace;
        try (Reader reader = IOHelper.newReader(workspaceFilePath)) {
            workspace = objectMapper.readValue(reader, MirrorWorkspaceProperties.class);
        }
        Path workspacePath = directoriesService.getMirrorHelperWorkspaceDir();
        if (Files.exists(workspacePath)) {
            log.warn("THIS ACTION DELETE ALL FILES IN {}", workspacePath);
            IOHelper.deleteDir(workspacePath, false);
        } else {
            Files.createDirectories(workspacePath);
        }
        applyWorkspace(workspace);
        log.info("Complete");
    }

    private void applyWorkspace(MirrorWorkspaceProperties workspace) throws Exception {
        Path workspacePath = directoriesService.getMirrorHelperWorkspaceDir();
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

                restTemplate.execute(l.getUrl(), HttpMethod.GET, null, clientHttpResponse -> {
                    File ret = new File(String.valueOf(tmpPath));
                    FileOutputStream fileIO = new FileOutputStream(ret);
                    StreamUtils.copy(clientHttpResponse.getBody(), fileIO);
                    fileIO.close();
                    return ret;
                });

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
                IOHelper.createParentDirs(target);
                restTemplate.execute(e.getValue().getUrl(), HttpMethod.GET, null, clientHttpResponse -> {
                    File ret = new File(String.valueOf(target));
                    ret.createNewFile();
                    FileOutputStream fileIo = new FileOutputStream(ret);
                    StreamUtils.copy(clientHttpResponse.getBody(), fileIo);
                    fileIo.close();
                    return ret;
                });
            }
            log.info("Install lwjgl3 directory");
            lwjglDownloadService.lwjglDownload(workspace.getLwjgl3version(), "mirrorhelper-tmp-lwjgl3");
            Path lwjgl3Path = workspacePath.resolve("workdir").resolve("lwjgl3");
            IOHelper.move(directoriesService.getUpdatesDir().resolve("mirrorhelper-tmp-lwjgl3"), lwjgl3Path);
            Files.deleteIfExists(directoriesService.getUpdatesDir().resolve("mirrorhelper-tmp-lwjgl3"));
            log.info("Save config");
        } finally {
            IOHelper.deleteDir(tmp, true);
        }
    }
}
