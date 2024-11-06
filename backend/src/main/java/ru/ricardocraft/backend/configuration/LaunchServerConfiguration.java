package ru.ricardocraft.backend.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.LaunchServerBuilder;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.JLineCommandHandler;
import ru.ricardocraft.backend.command.utls.StdCommandHandler;
import ru.ricardocraft.backend.config.BasicLaunchServerConfigManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.config.LaunchServerConfigManager;
import ru.ricardocraft.backend.properties.LaunchServerEnv;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.core.LauncherTrustManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.manangers.CertificateManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.security.cert.CertificateException;

@Configuration
@RequiredArgsConstructor
public class LaunchServerConfiguration {

    private static final Logger logger = LogManager.getLogger();

    LaunchServerEnv env = LaunchServerEnv.PRODUCTION;

    Path dir = IOHelper.WORKING_DIR;

    @PostConstruct
    public void init() {
        JVMHelper.verifySystemProperties(LaunchServer.class, false);
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public LaunchServerDirectories directories() {
        LaunchServerDirectories directories = new LaunchServerDirectories();

        directories.dir = dir;
        directories.collect();

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
    public LaunchServerConfig launchServerConfig() throws IOException {
        Path configFile;
        configFile = dir.resolve("LaunchServer.json");

        IOHelper.generateConfigIfNotExists(configFile, env);
        logger.info("Reading LaunchServer config file");
        BufferedReader reader = IOHelper.newReader(configFile);
        return Launcher.gsonManager.gson.fromJson(reader, LaunchServerConfig.class);
    }

    @Bean
    public LaunchServerRuntimeConfig runtimeConfig() throws IOException {
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
                runtimeConfig = Launcher.gsonManager.gson.fromJson(reader, LaunchServerRuntimeConfig.class);
            }
        }

        return runtimeConfig;
    }

    @Bean
    public LaunchServerConfigManager launchServerConfigManager() {
        Path runtimeConfigFile = dir.resolve("RuntimeLaunchServer.json");
        Path configFile = dir.resolve("LaunchServer.json");

        return new BasicLaunchServerConfigManager(configFile, runtimeConfigFile);
    }

    @Bean
    public LaunchServer launchServer(LaunchServerDirectories directories,
                                     CertificateManager certificateManager,
                                     CommandHandler commandHandler,
                                     LaunchServerConfig config,
                                     LaunchServerRuntimeConfig runtimeConfig,
                                     LaunchServerConfigManager launchServerConfigManager) throws Exception {

        return new LaunchServerBuilder()
                .setDirectories(directories)
                .setEnv(env)
                .setCommandHandler(commandHandler)
                .setRuntimeConfig(runtimeConfig)
                .setConfig(config)
                .setLaunchServerConfigManager(launchServerConfigManager)
                .setCertificateManager(certificateManager)
                .build();
    }
}
