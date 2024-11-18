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
import ru.ricardocraft.backend.base.ProGuard;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.base.helper.SignHelper;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

@Component
public class SecurityCheckCommand extends Command {

    private static final Logger logger = LogManager.getLogger(SecurityCheckCommand.class);

    private final transient LaunchServerConfig config;
    private final transient LaunchServerDirectories directories;
    private final transient AuthProviders authProviders;
    private final transient ProtectHandler protectHandler;
    private final transient ProfileProvider profileProvider;
    private final transient ProGuard proGuard;

    @Autowired
    public SecurityCheckCommand(LaunchServerConfig config,
                                LaunchServerDirectories directories,
                                AuthProviders authProviders,
                                ProtectHandler protectHandler,
                                ProfileProvider profileProvider,
                                ProGuard proGuard) {
        super();

        this.config = config;
        this.directories = directories;
        this.authProviders = authProviders;
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
        this.proGuard = proGuard;
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
        return "check configuration";
    }

    @Override
    public void invoke(String... args) {
        authProviders.getAuthProviders().forEach((name, pair) -> {
        });
        switch (protectHandler) {
            case NoProtectHandler noProtectHandler -> printCheckResult("protectHandler", "protectHandler none", false);
            case AdvancedProtectHandler advancedProtectHandler -> {
                printCheckResult("protectHandler", "", true);
                if (!config.advancedProtectHandlerConfig.enableHardwareFeature) {
                    printCheckResult("protectHandler.hardwareId", "you can improve security by using hwid provider", null);
                } else {
                    printCheckResult("protectHandler.hardwareId", "", true);
                }
            }
            case StdProtectHandler stdProtectHandler ->
                    printCheckResult("protectHandler", "you can improve security by using advanced", null);
            case null, default -> printCheckResult("protectHandler", "unknown protectHandler", null);
        }
        if (config.netty.address.startsWith("ws://")) {
            if (config.netty.ipForwarding)
                printCheckResult("netty.ipForwarding", "ipForwarding may be used to spoofing ip", null);
            printCheckResult("netty.address", "websocket connection not secure", false);
        } else if (config.netty.address.startsWith("wss://")) {
            if (!config.netty.ipForwarding)
                printCheckResult("netty.ipForwarding", "ipForwarding not enabled. authLimiter may be get incorrect ip", null);
            printCheckResult("netty.address", "", true);
        }

        if (config.netty.launcherURL.startsWith("http://")) {
            printCheckResult("netty.launcherUrl", "launcher jar download connection not secure", false);
        } else if (config.netty.launcherURL.startsWith("https://")) {
            printCheckResult("netty.launcherUrl", "", true);
        }

        if (config.netty.launcherEXEURL.startsWith("http://")) {
            printCheckResult("netty.launcherExeUrl", "launcher exe download connection not secure", false);
        } else if (config.netty.launcherEXEURL.startsWith("https://")) {
            printCheckResult("netty.launcherExeUrl", "", true);
        }

        if (config.netty.downloadURL.startsWith("http://")) {
            printCheckResult("netty.downloadUrl", "assets/clients download connection not secure", false);
        } else if (config.netty.downloadURL.startsWith("https://")) {
            printCheckResult("netty.downloadUrl", "", true);
        }

        if (!config.sign.enabled) {
            printCheckResult("sign", "it is recommended to use a signature", null);
        } else {
            boolean bad = false;
            try {
                KeyStore keyStore = SignHelper.getStore(new File(config.sign.keyStore).toPath(), config.sign.keyStorePass, config.sign.keyStoreType);
                Certificate[] certChainPlain = keyStore.getCertificateChain(config.sign.keyAlias);
                List<X509Certificate> certChain = Arrays.stream(certChainPlain).map(e -> (X509Certificate) e).toList();
                X509Certificate cert = certChain.getFirst();
                cert.checkValidity();
                if (certChain.size() == 1) {
                    printCheckResult("sign", "certificate chain contains <2 element(recommend 2 and more)", false);
                    bad = true;
                }
                if ((cert.getBasicConstraints() & 1) == 1) {
                    printCheckResult("sign", "end certificate - CA", false);
                    bad = true;
                }
                for (X509Certificate certificate : certChain) {
                    certificate.checkValidity();
                }
            } catch (Throwable e) {
                logger.error("Sign check failed", e);
                bad = true;
            }
            if (!bad)
                printCheckResult("sign", "", true);
        }

        if (proGuard.enabled) {
            printCheckResult("launcher.enabledProGuard", "proguard not enabled", false);
        } else {
            printCheckResult("launcher.enabledProGuard", "", true);
        }
        if (!config.launcher.stripLineNumbers) {
            printCheckResult("launcher.stripLineNumbers", "stripLineNumbers not enabled", false);
        } else {
            printCheckResult("launcher.stripLineNumbers", "", true);
        }

        switch (config.env) {
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

        //Linux permissions check
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            try {
                int uid = 0, gid = 0;
                String[] status = new String(IOHelper.read(Paths.get("/proc/self/status"))).split("\n");
                for (String line : status) {
                    String[] parts = line.split(":");
                    if (parts.length == 0) continue;
                    if (parts[0].trim().equalsIgnoreCase("Uid")) {
                        String[] words = parts[1].trim().split(" ");
                        uid = Integer.parseInt(words[0]);
                        if (Integer.parseInt(words[0]) == 0 || Integer.parseInt(words[0]) == 0) {
                            logger.error("The process is started as root! It is not recommended");
                        }
                    }
                    if (parts[0].trim().equalsIgnoreCase("Gid")) {
                        String[] words = parts[1].trim().split(" ");
                        gid = Integer.parseInt(words[0]);
                        if (Integer.parseInt(words[0]) == 0 || Integer.parseInt(words[0]) == 0) {
                            logger.error("The process is started as root group! It is not recommended");
                        }
                    }
                }
                if (checkOtherWriteAccess(IOHelper.getCodeSource(LaunchServer.class))) {
                    logger.warn("Write access to LaunchServer.jar. Please use 'chmod 755 LaunchServer.jar'");
                }
                if (Files.exists(this.directories.dir.resolve(".keys")) && checkOtherReadOrWriteAccess(this.directories.dir.resolve(".keys"))) {
                    logger.warn("Write or read access to .keys directory. Please use 'chmod -R 600 .keys'");
                }
                if (Files.exists(this.directories.dir.resolve("LaunchServerConfig.json")) && checkOtherReadOrWriteAccess(this.directories.dir.resolve("LaunchServerConfig.json"))) {
                    logger.warn("Write or read access to LaunchServerConfig.json. Please use 'chmod 600 LaunchServerConfig.json'");
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
