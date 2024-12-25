package ru.ricardocraft.backend.command.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.auth.protect.NoProtectHandler;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.StdProtectHandler;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.StringTokenizer;

@Component
public class SecurityCheckCommand extends Command {

    private static final Logger logger = LogManager.getLogger(SecurityCheckCommand.class);

    private final transient LaunchServerProperties config;
    private final transient DirectoriesManager directoriesManager;
    private final transient NettyProperties nettyProperties;
    private final transient AuthProviders authProviders;
    private final transient ProtectHandler protectHandler;
    private final transient ProfileProvider profileProvider;

    @Autowired
    public SecurityCheckCommand(LaunchServerProperties config,
                                DirectoriesManager directoriesManager,
                                NettyProperties nettyProperties,
                                AuthProviders authProviders,
                                ProtectHandler protectHandler,
                                ProfileProvider profileProvider) {
        super();

        this.config = config;
        this.directoriesManager = directoriesManager;
        this.nettyProperties = nettyProperties;
        this.authProviders = authProviders;
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
    }

    public static void printCheckResult(String module, String comment, Boolean status) {
        if (status == null) {
            logger.warn("[%s] %s".formatted(module, comment));
        } else if (status) {
            logger.info("[%s] %s OK".formatted(module, comment));
        } else {
            logger.error("[%s] %s".formatted(module, comment));
        }
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "multiModCheck configuration";
    }

    @Override
    public void invoke(String... args) {
        authProviders.getAuthProviders().forEach((name, pair) -> {
        });
        switch (protectHandler) {
            case NoProtectHandler noProtectHandler -> printCheckResult("protectHandler", "protectHandler none", false);
            case AdvancedProtectHandler advancedProtectHandler -> {
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
        if (nettyProperties.getAddress().startsWith("ws://")) {
            if (nettyProperties.getIpForwarding())
                printCheckResult("netty.ipForwarding", "ipForwarding may be used to spoofing ip", null);
            printCheckResult("netty.address", "websocket connection not secure", false);
        } else if (nettyProperties.getAddress().startsWith("wss://")) {
            if (!nettyProperties.getIpForwarding())
                printCheckResult("netty.ipForwarding", "ipForwarding not enabled. authLimiter may be get incorrect ip", null);
            printCheckResult("netty.address", "", true);
        }

        if (nettyProperties.getLauncherURL().startsWith("http://")) {
            printCheckResult("netty.launcherUrl", "launcher jar download connection not secure", false);
        } else if (nettyProperties.getLauncherURL().startsWith("https://")) {
            printCheckResult("netty.launcherUrl", "", true);
        }

        if (nettyProperties.getLauncherEXEURL().startsWith("http://")) {
            printCheckResult("netty.launcherExeUrl", "launcher exe download connection not secure", false);
        } else if (nettyProperties.getLauncherEXEURL().startsWith("https://")) {
            printCheckResult("netty.launcherExeUrl", "", true);
        }

        if (nettyProperties.getDownloadURL().startsWith("http://")) {
            printCheckResult("netty.downloadUrl", "assets/clients download connection not secure", false);
        } else if (nettyProperties.getDownloadURL().startsWith("https://")) {
            printCheckResult("netty.downloadUrl", "", true);
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
                            logger.error("The process is started as root! It is not recommended");
                        }
                    }
                    if (parts[0].trim().equalsIgnoreCase("Gid")) {
                        String[] words = parts[1].trim().split(" ");
                        if (Integer.parseInt(words[0]) == 0) {
                            logger.error("The process is started as root group! It is not recommended");
                        }
                    }
                }
                if (checkOtherWriteAccess(IOHelper.getCodeSource(LaunchServer.class))) {
                    logger.warn("Write access to LaunchServer.jar. Please use 'chmod 755 LaunchServer.jar'");
                }
                if (checkOtherReadOrWriteAccess(this.directoriesManager.getKeyDirectoryDir())) {
                    logger.warn("Write or read access to .keys directory. Please use 'chmod -R 600 .keys'");
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
        logger.info("Check completed");
    }

    public boolean checkOtherWriteAccess(Path file) throws IOException {
        Set<PosixFilePermission> permissionSet = Files.getPosixFilePermissions(file);
        return permissionSet.contains(PosixFilePermission.OTHERS_WRITE);
    }

    public boolean checkOtherReadOrWriteAccess(Path file) throws IOException {
        Set<PosixFilePermission> permissionSet = Files.getPosixFilePermissions(file);
        return permissionSet.contains(PosixFilePermission.OTHERS_WRITE) || permissionSet.contains(PosixFilePermission.OTHERS_READ);
    }
}
