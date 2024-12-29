package ru.ricardocraft.backend.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;
import ru.ricardocraft.backend.service.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.NoProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.StdProtectHandler;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.StringTokenizer;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class SecurityCheckCommand {

    private final LaunchServerProperties config;
    private final DirectoriesService directoriesService;
    private final HttpServerProperties httpServerProperties;
    private final AuthProviders authProviders;
    private final ProtectHandler protectHandler;
    private final ProfileProvider profileProvider;

    @ShellMethod("[] multiModCheck configuration")
    public void securityCheck() {
        authProviders.getAuthProviders().forEach((name, pair) -> {});
        switch (protectHandler) {
            case NoProtectHandler ignored -> printCheckResult("protectHandler", "protectHandler none", false);
            case AdvancedProtectHandler ignored -> {
                printCheckResult("protectHandler", "", true);
                if (!config.getAdvancedProtectHandler().getEnableHardwareFeature()) {
                    printCheckResult("protectHandler.hardwareId", "you can improve security by using hwid provider", null);
                } else {
                    printCheckResult("protectHandler.hardwareId", "", true);
                }
            }
            case StdProtectHandler stdProtectHandler ->
                    printCheckResult("protectHandler", "you can improve security by using advanced", null);
            case null, default -> printCheckResult("protectHandler", "unknown protectHandler", null);
        }
        if (httpServerProperties.getAddress().startsWith("ws://")) {
            if (httpServerProperties.getIpForwarding())
                printCheckResult("server.ipForwarding", "ipForwarding may be used to spoofing ip", null);
            printCheckResult("server.address", "websocket connection not secure", false);
        } else if (httpServerProperties.getAddress().startsWith("wss://")) {
            if (!httpServerProperties.getIpForwarding())
                printCheckResult("server.ipForwarding", "ipForwarding not enabled. authLimiter may be get incorrect ip", null);
            printCheckResult("server.address", "", true);
        }

        if (httpServerProperties.getLauncherURL().startsWith("http://")) {
            printCheckResult("server.launcherUrl", "launcher jar download connection not secure", false);
        } else if (httpServerProperties.getLauncherURL().startsWith("https://")) {
            printCheckResult("server.launcherUrl", "", true);
        }

        if (httpServerProperties.getLauncherEXEURL().startsWith("http://")) {
            printCheckResult("server.launcherExeUrl", "launcher exe download connection not secure", false);
        } else if (httpServerProperties.getLauncherEXEURL().startsWith("https://")) {
            printCheckResult("server.launcherExeUrl", "", true);
        }

        if (httpServerProperties.getDownloadURL().startsWith("http://")) {
            printCheckResult("server.downloadUrl", "assets/clients download connection not secure", false);
        } else if (httpServerProperties.getDownloadURL().startsWith("https://")) {
            printCheckResult("server.downloadUrl", "", true);
        }

        if (config.getProguard().getEnabled()) {
            printCheckResult("launcher.enabledProGuard", "proguard not enabled", false);
        } else {
            printCheckResult("launcher.enabledProGuard", "", true);
        }
        if (!config.getLauncher().getStripLineNumbers()) {
            printCheckResult("launcher.stripLineNumbers", "stripLineNumbers not enabled", false);
        } else {
            printCheckResult("launcher.stripLineNumbers", "", true);
        }

        switch (config.getEnv()) {
            case DEV -> printCheckResult("env", "found env DEV", false);
            case DEBUG -> printCheckResult("env", "found env DEBUG", false);
            case STD -> printCheckResult("env", "you can improve security by using env PROD", null);
            case PROD -> printCheckResult("env", "", true);
        }

        //Profiles
        for (ClientProfile profile : profileProvider.getProfiles()) {
            boolean bad = false;
            String profileModuleName = "profiles.%s".formatted(profile.getTitle());
            for (String exc : profile.getUpdateExclusions()) {
                StringTokenizer tokenizer = new StringTokenizer(exc, "/");
                if (exc.endsWith(".jar")) {
                    printCheckResult(profileModuleName, "updateExclusions %s not safe. Cheats may be injected very easy!".formatted(exc), false);
                    bad = true;
                    continue;
                }
                if (tokenizer.hasMoreTokens() && tokenizer.nextToken().equals("mods")) {
                    String nextToken = tokenizer.nextToken();
                    if (!tokenizer.hasMoreTokens()) {
                        if (!exc.endsWith("/")) {
                            printCheckResult(profileModuleName, "updateExclusions %s not safe. Cheats may be injected very easy!".formatted(exc), false);
                            bad = true;
                        }
                    } else {
                        if (nextToken.equals("memory_repo") || nextToken.equals(profile.getVersion().toString())) {
                            printCheckResult(profileModuleName, "updateExclusions %s not safe. Cheats may be injected very easy!".formatted(exc), false);
                            bad = true;
                        }
                    }
                }
            }
            if (!bad)
                printCheckResult(profileModuleName, "", true);
        }

        //Linux permissions multiModCheck
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            try {
                String[] status = new String(IOHelper.read(Paths.get("/proc/self/status"))).split("\n");
                for (String line : status) {
                    String[] parts = line.split(":");
                    if (parts.length == 0) continue;
                    if (parts[0].trim().equalsIgnoreCase("Uid")) {
                        String[] words = parts[1].trim().split(" ");
                        if (Integer.parseInt(words[0]) == 0) {
                            log.error("The process is started as root! It is not recommended");
                        }
                    }
                    if (parts[0].trim().equalsIgnoreCase("Gid")) {
                        String[] words = parts[1].trim().split(" ");
                        if (Integer.parseInt(words[0]) == 0) {
                            log.error("The process is started as root group! It is not recommended");
                        }
                    }
                }
                if (checkOtherReadOrWriteAccess(this.directoriesService.getKeyDirectoryDir())) {
                    log.warn("Write or read access to .keys directory. Please use 'chmod -R 600 .keys'");
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        log.info("Check completed");
    }

    private void printCheckResult(String module, String comment, Boolean status) {
        if (status == null) {
            log.warn("[%s] %s".formatted(module, comment));
        } else if (status) {
            log.info("[%s] %s OK".formatted(module, comment));
        } else {
            log.error("[%s] %s".formatted(module, comment));
        }
    }

    private boolean checkOtherWriteAccess(Path file) throws IOException {
        Set<PosixFilePermission> permissionSet = Files.getPosixFilePermissions(file);
        return permissionSet.contains(PosixFilePermission.OTHERS_WRITE);
    }

    private boolean checkOtherReadOrWriteAccess(Path file) throws IOException {
        Set<PosixFilePermission> permissionSet = Files.getPosixFilePermissions(file);
        return permissionSet.contains(PosixFilePermission.OTHERS_WRITE) || permissionSet.contains(PosixFilePermission.OTHERS_READ);
    }
}
