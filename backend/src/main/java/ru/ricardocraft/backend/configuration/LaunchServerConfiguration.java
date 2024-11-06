package ru.ricardocraft.backend.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.LaunchServerBuilder;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.mix.MixProvider;
import ru.ricardocraft.backend.auth.password.PasswordVerifier;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.base.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.JLineCommandHandler;
import ru.ricardocraft.backend.command.utls.StdCommandHandler;
import ru.ricardocraft.backend.components.Component;
import ru.ricardocraft.backend.config.LaunchServerConfig;
import ru.ricardocraft.backend.config.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.core.LauncherTrustManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.helper.LogHelper;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.LaunchServerGsonManager;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.security.cert.CertificateException;

@Configuration
public class LaunchServerConfiguration {

    private static final Logger logger = LogManager.getLogger();

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
    public LaunchServer launchServer(CommandHandler commandHandler) throws Exception {

        JVMHelper.verifySystemProperties(LaunchServer.class, false);

        LogHelper.printVersion("LaunchServer");
        LogHelper.printLicense("LaunchServer");
        Path dir = IOHelper.WORKING_DIR;
        Path configFile, runtimeConfigFile;
        try {
            Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
            Security.addProvider(new BouncyCastleProvider());
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            LogHelper.error("Library BouncyCastle not found! Is directory 'libraries' empty?");
            throw new Exception();
        }
        LaunchServer.LaunchServerDirectories directories = new LaunchServer.LaunchServerDirectories();
        directories.dir = dir;
        directories.collect();
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

        LaunchServerRuntimeConfig runtimeConfig;
        LaunchServerConfig config;
        LaunchServer.LaunchServerEnv env = LaunchServer.LaunchServerEnv.PRODUCTION;

        registerAll();
        initGson();

        configFile = dir.resolve("LaunchServer.json");
        runtimeConfigFile = dir.resolve("RuntimeLaunchServer.json");

        generateConfigIfNotExists(configFile, env);
        logger.info("Reading LaunchServer config file");
        try (BufferedReader reader = IOHelper.newReader(configFile)) {
            config = Launcher.gsonManager.gson.fromJson(reader, LaunchServerConfig.class);
        }
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

        LaunchServer.LaunchServerConfigManager launchServerConfigManager = new LaunchServerConfiguration.BasicLaunchServerConfigManager(configFile, runtimeConfigFile);
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

    public static void initGson() {
        Launcher.gsonManager = new LaunchServerGsonManager();
        Launcher.gsonManager.initGson();
    }

    public static void registerAll() {
        AuthCoreProvider.registerProviders();
        PasswordVerifier.registerProviders();
        TextureProvider.registerProviders();
        Component.registerComponents();
        ProtectHandler.registerHandlers();
        WebSocketService.registerResponses();
        AuthRequest.registerProviders();
        GetAvailabilityAuthRequest.registerProviders();
        OptionalAction.registerProviders();
        OptionalTrigger.registerProviders();
        MixProvider.registerProviders();
        ProfileProvider.registerProviders();
        UpdatesProvider.registerProviders();
    }

    public static void generateConfigIfNotExists(Path configFile, LaunchServer.LaunchServerEnv env) throws IOException {
        if (IOHelper.isFile(configFile))
            return;

        // Create new config
        logger.info("Creating LaunchServer config");


        LaunchServerConfig newConfig = LaunchServerConfig.getDefault(env);
        // Set server address
        String address;
        if (env.equals(LaunchServer.LaunchServerEnv.TEST)) {
            address = "localhost";
            newConfig.setProjectName("test");
        } else {
            address = System.getenv("ADDRESS");
            if (address == null) {
                address = System.getProperty("launchserver.address", null);
            }
            String projectName = System.getenv("PROJECTNAME");
            if (projectName == null) {
                projectName = System.getProperty("launchserver.projectname", null);
            }
            newConfig.setProjectName(projectName);
        }
        if (address == null || address.isEmpty()) {
            address = "localhost:9274";
        }
        if (newConfig.projectName == null || newConfig.projectName.isEmpty()) {
            newConfig.projectName = "ricardocraft";
        }
        int port = 9274;
        if(address.contains(":")) {
            String portString = address.substring(address.indexOf(':')+1);
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                logger.warn("Unknown port {}, using 9274", portString);
            }
        } else {
            logger.info("Address {} doesn't contains port (you want to use nginx?)", address);
        }
        newConfig.netty.address = "ws://" + address + "/api";
        newConfig.netty.downloadURL = "http://" + address + "/%dirname%/";
        newConfig.netty.launcherURL = "http://" + address + "/Launcher.jar";
        newConfig.netty.launcherEXEURL = "http://" + address + "/Launcher.exe";
        newConfig.netty.binds[0].port = port;

        // Write LaunchServer config
        logger.info("Writing LaunchServer config file");
        try (BufferedWriter writer = IOHelper.newWriter(configFile)) {
            Launcher.gsonManager.configGson.toJson(newConfig, writer);
        }
    }

    private static class BasicLaunchServerConfigManager implements LaunchServer.LaunchServerConfigManager {
        private final Path configFile;
        private final Path runtimeConfigFile;

        public BasicLaunchServerConfigManager(Path configFile, Path runtimeConfigFile) {
            this.configFile = configFile;
            this.runtimeConfigFile = runtimeConfigFile;
        }

        @Override
        public LaunchServerConfig readConfig() throws IOException {
            LaunchServerConfig config1;
            try (BufferedReader reader = IOHelper.newReader(configFile)) {
                config1 = Launcher.gsonManager.gson.fromJson(reader, LaunchServerConfig.class);
            }
            return config1;
        }

        @Override
        public LaunchServerRuntimeConfig readRuntimeConfig() throws IOException {
            LaunchServerRuntimeConfig config1;
            try (BufferedReader reader = IOHelper.newReader(runtimeConfigFile)) {
                config1 = Launcher.gsonManager.gson.fromJson(reader, LaunchServerRuntimeConfig.class);
            }
            return config1;
        }

        @Override
        public void writeConfig(LaunchServerConfig config) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (Writer writer = IOHelper.newWriter(output)) {
                if (Launcher.gsonManager.configGson != null) {
                    Launcher.gsonManager.configGson.toJson(config, writer);
                } else {
                    logger.error("Error writing LaunchServer config file. Gson is null");
                }
            }
            byte[] bytes = output.toByteArray();
            if(bytes.length > 0) {
                IOHelper.write(configFile, bytes);
            }
        }

        @Override
        public void writeRuntimeConfig(LaunchServerRuntimeConfig config) throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (Writer writer = IOHelper.newWriter(output)) {
                if (Launcher.gsonManager.configGson != null) {
                    Launcher.gsonManager.configGson.toJson(config, writer);
                } else {
                    logger.error("Error writing LaunchServer runtime config file. Gson is null");
                }
            }
            byte[] bytes = output.toByteArray();
            if(bytes.length > 0) {
                IOHelper.write(runtimeConfigFile, bytes);
            }
        }
    }
}
