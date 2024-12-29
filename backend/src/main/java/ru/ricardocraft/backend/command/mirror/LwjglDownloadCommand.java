package ru.ricardocraft.backend.command.mirror;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.service.DirectoriesService;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class LwjglDownloadCommand {

    private final DirectoriesService directoriesService;

    @ShellMethod("[version] [client dir] download lwjgl 3.3.0+")
    public void lwjglDownload(@ShellOption String version,
                              @ShellOption String clientDirectory) throws Exception {
        Path clientDir = directoriesService.getUpdatesDir().resolve(clientDirectory);
        Path lwjglDir = clientDir.resolve("libraries").resolve("org").resolve("lwjgl");
        Path natives = clientDir.resolve("natives");
        List<String> components = List.of("lwjgl", "lwjgl-stb", "lwjgl-opengl", "lwjgl-openal", "lwjgl-glfw", "lwjgl-tinyfd", "lwjgl-jemalloc");
        List<String> archs = List.of("linux", "windows", "windows-x86", "windows-arm64", "macos", "macos-arm64", "linux-arm64", "linux-arm32");
        String mirror = "https://repo1.maven.org/maven2/org/lwjgl/";
        for (String component : components) {
            Path jarPath = lwjglDir.resolve(component).resolve(version).resolve(component.concat("-").concat(version).concat(".jar"));
            Path jarDirPath = jarPath.getParent();
            Files.createDirectories(jarDirPath);
            String prepareUrl = mirror
                    .concat(component)
                    .concat("/")
                    .concat(version)
                    .concat("/");
            URL jarUrl = new URI(prepareUrl
                    .concat("%s-%s.jar".formatted(component, version))).toURL();
            log.info("Download {} to {}", jarUrl, jarPath);
            download(jarUrl, jarPath);
            for (String arch : archs) {
                URL nativesUrl = new URI(prepareUrl
                        .concat("%s-%s-natives-%s.jar".formatted(component, version, arch))).toURL();
                var pair = getFromLwjglNativeName(arch);
                Path nativesPath = natives.resolve(pair.os.name.toLowerCase()).resolve(pair.arch.name.toLowerCase());
                IOHelper.createParentDirs(nativesPath);
                log.info("Download natives {}", nativesUrl);
                List<String> processedFiles = new ArrayList<>();
                try (ZipInputStream input = new ZipInputStream(IOHelper.newInput(nativesUrl))) {
                    ZipEntry entry = input.getNextEntry();
                    while (entry != null) {
                        if (!entry.isDirectory() && !entry.getName().startsWith("META-INF")
                                && !entry.getName().endsWith(".sha1") && !entry.getName().endsWith(".git")) {
                            Path path = Paths.get(entry.getName());
                            String filename = path.getFileName().toString();
                            log.info("Process {}", filename);
                            if (processedFiles.contains(filename)) {
                                if ("windows-x86".equals(arch)) {
                                    String oldName = filename;
                                    int index = filename.indexOf(".");
                                    filename = filename.substring(0, index).concat("32").concat(filename.substring(index));
                                    log.info("Change name {} to {}", oldName, filename);
                                } else {
                                    log.warn("Duplicate {}", filename);
                                }
                            }
                            IOHelper.transfer(input, nativesPath.resolve(filename));
                            processedFiles.add(filename);
                        }
                        entry = input.getNextEntry();
                    }
                } catch (FileNotFoundException e) {
                    log.warn("Skip {}", nativesUrl);
                }
            }
            log.info("Complete");
        }
    }

    private OSArchPair getFromLwjglNativeName(String name) {
        int pos = name.indexOf("-");
        JVMHelper.ARCH arch;
        JVMHelper.OS os;
        if (pos < 0) {
            arch = JVMHelper.ARCH.X86_64;
            os = getOS(name);
        } else {
            os = getOS(name.substring(0, pos));
            arch = getArch(name.substring(pos + 1));
        }
        return new OSArchPair(arch, os);
    }

    private JVMHelper.ARCH getArch(String name) {
        return switch (name) {
            case "x86" -> JVMHelper.ARCH.X86;
            case "arm64" -> JVMHelper.ARCH.ARM64;
            case "arm32" -> JVMHelper.ARCH.ARM32;
            case "x86-64" -> JVMHelper.ARCH.X86_64;
            default -> throw new IllegalArgumentException(name);
        };
    }

    private JVMHelper.OS getOS(String name) {
        return switch (name) {
            case "windows" -> JVMHelper.OS.WINDOWS;
            case "linux" -> JVMHelper.OS.LINUX;
            case "macos" -> JVMHelper.OS.MACOSX;
            default -> throw new IllegalArgumentException(name);
        };
    }

    private void download(URL url, Path target) throws IOException {
        if (Files.exists(target)) return;
        try (InputStream input = IOHelper.newInput(url)) {
            try (OutputStream output = new FileOutputStream(target.toFile())) {
                IOHelper.transfer(input, output);
            }
        }
    }

    public record OSArchPair(JVMHelper.ARCH arch, JVMHelper.OS os) {

    }
}
