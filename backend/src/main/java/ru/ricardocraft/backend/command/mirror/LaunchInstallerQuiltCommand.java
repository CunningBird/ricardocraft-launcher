package ru.ricardocraft.backend.command.mirror;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.UpdatesService;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class LaunchInstallerQuiltCommand {

    private final DirectoriesService directoriesService;
    private final UpdatesService updatesService;
    private final ObjectMapper objectMapper;

    @ShellMethod("[minecraft version] [vanilla dir] [fabric installer file] install quilt to client")
    public void launchInstallerQuilit(@ShellOption String version,
                                      @ShellOption String installerVanillaDir,
                                      @ShellOption String installerFabricFile) throws Exception {
        Path vanillaDir = directoriesService.getUpdatesDir().resolve(installerVanillaDir);
        if (!Files.exists(vanillaDir)) {
            throw new FileNotFoundException(vanillaDir.toString());
        }
        Path fabricInstallerFile = Paths.get(installerFabricFile);
        List<String> processArgs = new ArrayList<>(6);
        processArgs.add(IOHelper.resolveJavaBin(IOHelper.JVM_DIR).toString());
        processArgs.add("-jar");
        processArgs.add(fabricInstallerFile.toAbsolutePath().toString());
        processArgs.add("install");
        processArgs.add("client");
        processArgs.add(version);
        processArgs.add("--install-dir=".concat(vanillaDir.toString()));
        processArgs.add("--no-profile");
        log.debug("Launch {}", String.join(" ", processArgs));
        Process process = new ProcessBuilder(processArgs).inheritIO().start();
        process.waitFor();
        if (!Files.exists(vanillaDir.resolve("versions"))) {
            throw new FileNotFoundException("versions not found. Quilt not installed");
        }
        Path fabricClientDir = Files.list(vanillaDir.resolve("versions")).findFirst().orElseThrow();
        Path fabricProfileFile = Files.list(fabricClientDir).filter(p -> p.getFileName().toString().endsWith(".json")).findFirst().orElseThrow();
        log.debug("Quilt profile {}", fabricProfileFile);
        MinecraftProfile fabricProfile;
        try (Reader reader = IOHelper.newReader(fabricProfileFile)) {
            fabricProfile = objectMapper.readValue(reader, MinecraftProfile.class);
        }
        for (MinecraftProfileLibrary library : fabricProfile.libraries) {
            NamedURL url = makeURL(library.url, library.name);
            log.info("Download {} into {}", url.url.toString(), url.name);
            Path file = vanillaDir.resolve("libraries").resolve(url.name);
            IOHelper.createParentDirs(file);
            try (InputStream stream = IOHelper.newInput(url.url)) {
                try (OutputStream output = IOHelper.newOutput(file)) {
                    IOHelper.transfer(stream, output);
                }
            }
        }
        log.info("Clearing...");
        IOHelper.deleteDir(vanillaDir.resolve("versions"), true);
        updatesService.syncUpdatesDir(List.of(installerVanillaDir));
        log.info("Quilt installed successful. Please use `makeprofile` command");
    }

    private NamedURL makeURL(String mavenUrl, String mavenId) throws URISyntaxException, MalformedURLException {
        URI baseUri = new URI(mavenUrl);
        String scheme = baseUri.getScheme();
        String host = baseUri.getHost();
        int port = baseUri.getPort();
        if (port != -1)
            host = host + ":" + port;
        String path = baseUri.getPath();
        //
        String[] mavenIdSplit = mavenId.split(":");
        String artifact = "%s/%s/%s/%s-%s.jar".formatted(mavenIdSplit[0].replaceAll("\\.", "/"),
                mavenIdSplit[1], mavenIdSplit[2], mavenIdSplit[1], mavenIdSplit[2]);
        //
        URL url = new URI(scheme, host, path + artifact, "", "").toURL();
        return new NamedURL(url, artifact);
    }

    public static class MinecraftProfileLibrary {
        public String name;
        public String url; // maven repo
    }

    public static class MinecraftProfile {
        public List<MinecraftProfileLibrary> libraries;
    }

    public static class NamedURL {
        public final URL url;
        public final String name;

        public NamedURL(URL url, String name) {
            this.url = url;
            this.name = name;
        }
    }
}
