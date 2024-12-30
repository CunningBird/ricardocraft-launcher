package ru.ricardocraft.backend.service.command.mirror;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class LaunchInstallerForgeService {

    private final DirectoriesService directoriesService;
    private final ObjectMapper objectMapper;

    public void launchInstallerForge(String vanillaDir,String forgeInstallerFile) throws Exception {
        Path dir = directoriesService.getUpdatesDir().resolve(vanillaDir);
        if (!Files.exists(dir)) {
            throw new FileNotFoundException(dir.toString());
        }
        Path forgeInstaller = Paths.get(forgeInstallerFile);
        if (!Files.exists(forgeInstaller)) {
            throw new FileNotFoundException(forgeInstaller.toString());
        }
        log.info("If possible please consider moving to Fabric");
        log.info("Forge is supported by advertising when downloading and installing. Please do not use AdBlock when downloading it, this will help the project");
        log.error("FORGE INSTALLER COMMAND IS WORK IN PROGRESS!");

        ForgeInstallManifest forgeInstallManifest = null;
        try (ZipInputStream input = IOHelper.newZipInput(forgeInstaller)) {
            ZipEntry entry = input.getNextEntry();
            while (entry != null) {
                String filename = entry.getName();
                if (filename.equals("install_profile.json")) {
                    log.debug("Found install_profile.json");
                    forgeInstallManifest = readForgeInstallManifest(new NoClosingInputStream(input));
                }
                entry = input.getNextEntry();
            }
        }
        if (forgeInstallManifest == null) {
            throw new RuntimeException("Forge install manifest not found");
        }
        forgeInstallManifest.data.put("SIDE", new ServerAndClientValue("client"));
        forgeInstallManifest.data.put("MINECRAFT_JAR", new ServerAndClientValue(dir.resolve("minecraft.jar").toAbsolutePath().toString()));
        log.info("Collect install processors");
        for (InstallProcessor processor : forgeInstallManifest.processors) {
            processor.file = dir.resolve("tmp").resolve(forgeInstallManifest.findByName(processor.jar).downloads.artifact.path);
        }
        log.info("Launch pipeline");
        for (InstallProcessor processor : forgeInstallManifest.processors) {
            List<String> processArgs = new ArrayList<>();
            processArgs.add(IOHelper.resolveJavaBin(IOHelper.JVM_DIR).toString());
            processArgs.add("-jar");
            processArgs.add(processor.file.toAbsolutePath().toString());
            for (String arg : processor.args) {
                processArgs.add(forgeInstallManifest.dataReplace(arg));
            }
            log.debug("Launch {}", String.join(" ", processArgs));
            Process process = new ProcessBuilder(processArgs).inheritIO().start();
            process.waitFor();
        }
    }

    private ForgeInstallManifest readForgeInstallManifest(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream)) {
            return objectMapper.readValue(reader, ForgeInstallManifest.class);
        }
    }

    public static class LibraryArtifactInfo {
        public String path;
        public String url;
        public long size;
    }

    public static class LibraryDownloadInfo {
        public LibraryArtifactInfo artifact;
    }

    public static class LibraryInfo {
        public String name;
        public LibraryDownloadInfo downloads;
    }

    public static class ServerAndClientValue {
        public String client;
        public String server;

        public ServerAndClientValue(String client) {
            this.client = client;
        }
    }

    public static class InstallProcessor {
        public String jar; // maven id
        public transient Path file;
        public List<String> classpath;
        public List<String> args;
        public Map<String, String> outputs;
    }

    public static class ForgeInstallManifest {
        public Map<String, ServerAndClientValue> data;
        public List<InstallProcessor> processors;
        public List<LibraryInfo> libraries;

        public String dataReplace(String name) {
            ServerAndClientValue value = data.get(name);
            if (value == null) return name;
            return value.client;
        }

        public LibraryInfo findByName(String name) {
            for (LibraryInfo info : libraries) {
                if (name.equals(info.name)) {
                    return info;
                }
            }
            return null;
        }
    }

    public static class NoClosingInputStream extends InputStream {
        private final InputStream stream;

        public NoClosingInputStream(InputStream stream) {
            this.stream = stream;
        }

        public static InputStream nullInputStream() {
            return InputStream.nullInputStream();
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int read(@NotNull byte[] b) throws IOException {
            return stream.read(b);
        }

        @Override
        public int read(@NotNull byte[] b, int off, int len) throws IOException {
            return stream.read(b, off, len);
        }

        @Override
        public byte[] readAllBytes() throws IOException {
            return stream.readAllBytes();
        }

        @Override
        public byte[] readNBytes(int len) throws IOException {
            return stream.readNBytes(len);
        }

        @Override
        public int readNBytes(byte[] b, int off, int len) throws IOException {
            return stream.readNBytes(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }

        @Override
        public long transferTo(OutputStream out) throws IOException {
            return stream.transferTo(out);
        }

        @Override
        public void close() {
            // None
        }
    }
}
