package ru.ricardocraft.backend.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.JLineCommandHandler;
import ru.ricardocraft.backend.command.utls.StdCommandHandler;
import ru.ricardocraft.backend.core.LauncherTrustManager;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.manangers.BasicLaunchServerConfigManager;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.manangers.LaunchServerConfigManager;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerEnv;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@Configuration
@RequiredArgsConstructor
public class LaunchServerConfiguration {

    private static final Logger logger = LogManager.getLogger();

    private final Path dir = IOHelper.WORKING_DIR;

    @PostConstruct
    public void init() {
        JVMHelper.verifySystemProperties(LaunchServer.class, false);
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public LaunchServerEnv getEnv() {
        return LaunchServerEnv.PRODUCTION;
    }

    @Bean
    public LaunchServerDirectories directories() throws IOException {
        LaunchServerDirectories directories = new LaunchServerDirectories();

        directories.dir = dir;
        directories.collect();

        if (!Files.isDirectory(directories.launcherPackDir)) {
            Files.createDirectories(directories.launcherPackDir);
        }

        return directories;
    }

    @Bean
    public CertificateManager certificateManager() throws IOException {
        CertificateManager certificateManager = new CertificateManager();
        try {
            certificateManager.readTrustStore(dir.resolve("truststore"));
        } catch (CertificateException e) {
            throw new IOException(e);
        }
        {
            LauncherTrustManager.CheckClassResult result = certificateManager.checkClass(LaunchServer.class);
            if (result.type == LauncherTrustManager.CheckClassResultType.SUCCESS) {
                logger.info("LaunchServer signed by {}", result.endCertificate.getSubjectX500Principal().getName());
            } else if (result.type == LauncherTrustManager.CheckClassResultType.NOT_SIGNED) {
                // None
            } else {
                if (result.exception != null) {
                    logger.error(result.exception);
                }
                logger.warn("LaunchServer signed incorrectly. Status: {}", result.type.name());
            }
        }

        return new CertificateManager();
    }

    @Bean
    public CommandHandler commandHandler() {
        try {
            Class.forName("org.jline.terminal.Terminal");
            // JLine2 available
            CommandHandler commandHandler = new JLineCommandHandler();
            logger.info("JLine2 terminal enabled");
            return commandHandler;
        } catch (Exception ignored) {
            CommandHandler commandHandler = new StdCommandHandler(true);
            logger.warn("JLine2 isn't in classpath, using std");
            return commandHandler;
        }
    }

    @Bean
    public LaunchServerRuntimeConfig runtimeConfig(GsonManager gsonManager) throws IOException {
        LaunchServerRuntimeConfig runtimeConfig;

        Path runtimeConfigFile;
        runtimeConfigFile = dir.resolve("RuntimeLaunchServer.json");

        if (!Files.exists(runtimeConfigFile)) {
            logger.info("Reset LaunchServer runtime config file");
            runtimeConfig = new LaunchServerRuntimeConfig();
            runtimeConfig.reset();
        } else {
            logger.info("Reading LaunchServer runtime config file");
            try (BufferedReader reader = IOHelper.newReader(runtimeConfigFile)) {
                runtimeConfig = gsonManager.gson.fromJson(reader, LaunchServerRuntimeConfig.class);
            }
        }

        runtimeConfig.verify();

        return runtimeConfig;
    }

    @Bean
    public LaunchServerConfigManager launchServerConfigManager() {
        Path runtimeConfigFile = dir.resolve("RuntimeLaunchServer.json");
        Path configFile = dir.resolve("LaunchServer.json");

        return new BasicLaunchServerConfigManager(configFile, runtimeConfigFile);
    }

    @Bean
    public KeyAgreementManager keyAgreementManager(LaunchServerDirectories directories) throws IOException, InvalidKeySpecException {
        return new KeyAgreementManager(directories.keyDirectory);
    }
}
