package ru.ricardocraft.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.RejectAuthCoreProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.components.AuthLimiterComponent;
import ru.ricardocraft.backend.components.ProGuardComponent;
import ru.ricardocraft.backend.components.WhitelistComponent;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.helper.SignHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.*;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Component
public final class LaunchServer implements Runnable, AutoCloseable, Reconfigurable {

    private final Logger logger = LogManager.getLogger();

    public final AtomicBoolean started = new AtomicBoolean(false);
    public final ScheduledExecutorService service;

    public final Path dir;
    public final Path tmpDir;
    public final Path updatesDir;
    public final Path launcherLibraries;
    public final Path launcherLibrariesCompile;
    public final Path launcherPack;
    public final Path librariesDir;

    public LaunchServerConfig config;
    private final LaunchServerEnv env;
    public final LaunchServerRuntimeConfig runtime;
    public final CommandHandler commandHandler;

    public final LaunchServerConfigManager launchServerConfigManager;
    public final KeyAgreementManager keyAgreementManager;
    public final CertificateManager certificateManager;
    public final ReconfigurableManager reconfigurableManager;
    public final AuthHookManager authHookManager;
    public final MirrorManager mirrorManager;
    public final FeaturesManager featuresManager;
    public final AuthManager authManager;
    public final UpdatesManager updatesManager;

    public final JARLauncherBinary launcherBinary;
    public final EXELauncherBinary launcherEXEBinary;
    public final NettyServerSocketHandler nettyServerSocketHandler;

    public Map<String, ru.ricardocraft.backend.components.Component> components;

    @Autowired
    public LaunchServer(LaunchServerDirectories directories,
                        LaunchServerConfig config,
                        LaunchServerEnv env,
                        LaunchServerRuntimeConfig runtimeConfig,
                        LaunchServerConfigManager launchServerConfigManager,
                        KeyAgreementManager keyAgreementManager,
                        CommandHandler commandHandler,
                        CertificateManager certificateManager,
                        ReconfigurableManager reconfigurableManager,
                        AuthHookManager authHookManager,
                        MirrorManager mirrorManager,
                        FeaturesManager featuresManager,
                        AuthManager authManager,
                        UpdatesManager updatesManager,
                        JARLauncherBinary launcherBinary,
                        EXELauncherBinary launcherEXEBinary,
                        NettyServerSocketHandler nettyServerSocketHandler,

                        AuthLimiterComponent authLimiterComponent,
                        ProGuardComponent proGuardComponent,
                        WhitelistComponent whitelistComponent) throws IOException {

        this.service = Executors.newScheduledThreadPool(config.netty.performance.schedulerThread);

        this.dir = directories.dir;
        this.tmpDir = directories.tmpDir;
        this.updatesDir = directories.updatesDir;
        this.launcherLibraries = directories.launcherLibrariesDir;
        this.launcherLibrariesCompile = directories.launcherLibrariesCompileDir;
        this.launcherPack = directories.launcherPackDir;
        this.librariesDir = directories.librariesDir;

        this.config = config;
        this.env = env;
        this.runtime = runtimeConfig;
        this.commandHandler = commandHandler;

        this.launchServerConfigManager = launchServerConfigManager;
        this.keyAgreementManager = keyAgreementManager;
        this.certificateManager = certificateManager;
        this.reconfigurableManager = reconfigurableManager;
        this.authHookManager = authHookManager;
        this.mirrorManager = mirrorManager;
        this.featuresManager = featuresManager;
        this.authManager = authManager;
        this.updatesManager = updatesManager;

        this.launcherBinary = launcherBinary;
        this.launcherEXEBinary = launcherEXEBinary;

        this.nettyServerSocketHandler = nettyServerSocketHandler;

        this.components = new HashMap<>();
        this.components.put("authLimiter", authLimiterComponent);
        this.components.put("proguard", proGuardComponent);
        this.components.put("whitelist", whitelistComponent);

        init(config, ReloadType.FULL);

        if (config.sign.checkCertificateExpired) {
            checkCertificateExpired();
            service.scheduleAtFixedRate(this::checkCertificateExpired, 24, 24, TimeUnit.HOURS);
        }

        reconfigurableManager.registerObject("launchServer", this);
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        SubCommand reload = new SubCommand("[type]", "reload launchserver config") {
            @Override
            public void invoke(String... args) throws Exception {
                if (args.length == 0) {
                    reload(ReloadType.FULL);
                    return;
                }
                switch (args[0]) {
                    case "full" -> reload(ReloadType.FULL);
                    case "no_components" -> reload(ReloadType.NO_COMPONENTS);
                    default -> reload(ReloadType.NO_AUTH);
                }
            }
        };
        commands.put("reload", reload);
        SubCommand save = new SubCommand("[]", "save launchserver config") {
            @Override
            public void invoke(String... args) throws Exception {
                launchServerConfigManager.writeConfig(config);
                launchServerConfigManager.writeRuntimeConfig(runtime);
                logger.info("LaunchServerConfig saved");
            }
        };
        commands.put("save", save);
        SubCommand resetauth = new SubCommand("authId", "reset auth by id") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                AuthProviderPair pair = config.getAuthProviderPair(args[0]);
                if (pair == null) {
                    logger.error("Pair not found");
                    return;
                }
                pair.core.close();
                pair.core = new RejectAuthCoreProvider();
                pair.core.init(authManager, config, nettyServerSocketHandler, keyAgreementManager, pair);
            }
        };
        commands.put("resetauth", resetauth);
        return commands;
    }

    @Deprecated
    public Set<ClientProfile> getProfiles() {
        return config.profileProvider.getProfiles();
    }

    @Override
    public void run() {
        if (started.getAndSet(true))
            throw new IllegalStateException("LaunchServer has been already started");

        // Add shutdown hook, then start LaunchServer
        if (!this.env.equals(LaunchServerEnv.TEST)) {
            JVMHelper.RUNTIME.addShutdownHook(CommonHelper.newThread(null, false, () -> {
                try {
                    close();
                } catch (Exception e) {
                    logger.error("LaunchServer close error", e);
                }
            }));
            CommonHelper.newThread("Command Thread", true, commandHandler).start();
            // Sync updates dir
            CommonHelper.newThread("Profiles and updates sync", true, () -> {
                try {
                    // Sync profiles dir
                    config.profileProvider.syncProfilesDir(config, nettyServerSocketHandler);

                    // Sync updates dir
                    config.updatesProvider.syncInitially();
                } catch (IOException e) {
                    logger.error("Updates/Profiles not synced", e);
                }
            }).start();
        }
        if (config.netty != null) {
            nettyServerSocketHandler.close();
            CommonHelper.newThread("Netty Server Socket Thread", false, nettyServerSocketHandler).start();
        }

        logger.info("LaunchServer started");
    }

    public void close() throws Exception {
        service.shutdownNow();
        logger.info("Close server socket");
        nettyServerSocketHandler.close();
        // Close handlers & providers
        close(config, ReloadType.FULL);

        logger.info("Save LaunchServer runtime config");
        launchServerConfigManager.writeRuntimeConfig(runtime);
        // Print last message before death :(
        logger.info("LaunchServer stopped");
    }

    public void reload(ReloadType type) throws Exception {
        close(config, type);
        Map<String, AuthProviderPair> pairs = null;
        if (type.equals(ReloadType.NO_AUTH)) {
            pairs = config.auth;
        }
        logger.info("Reading LaunchServer config file");
        config = launchServerConfigManager.readConfig();
        if (type.equals(ReloadType.NO_AUTH)) {
            config.auth = pairs;
        }
        config.verify();
        init(config, type);
        if (!type.equals(ReloadType.NO_AUTH)) {
            nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((channel, wsHandler) -> {
                Client client = wsHandler.getClient();
                if (client.auth != null) {
                    client.auth = config.getAuthProviderPair(client.auth_id);
                }
            });
        }
    }

    public void init(LaunchServerConfig config, ReloadType type) {
        Launcher.applyLauncherEnv(config.env);
        for (Map.Entry<String, AuthProviderPair> provider : config.auth.entrySet()) {
            provider.getValue().init(authManager, config, nettyServerSocketHandler, keyAgreementManager, provider.getKey());
        }
        if (config.protectHandler != null) {
            reconfigurableManager.registerObject("protectHandler", config.protectHandler);
            config.protectHandler.init(config, keyAgreementManager);
        }
        if (config.profileProvider != null) {
            reconfigurableManager.registerObject("profileProvider", config.profileProvider);
            config.profileProvider.init(config.protectHandler);
        }
        if (config.updatesProvider != null) {
            reconfigurableManager.registerObject("updatesProvider", config.updatesProvider);
        }
        if (components != null) {
            components.forEach((k, v) -> reconfigurableManager.registerObject("component.".concat(k), v));
        }
        if (!type.equals(ReloadType.NO_AUTH)) {
            for (AuthProviderPair pair : config.auth.values()) {
                reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".core"), pair.core);
                reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
            }
        }
        Arrays.stream(config.mirrors).forEach(mirrorManager::addMirror);
    }

    public void close(LaunchServerConfig config, ReloadType type) {
        try {
            if (!type.equals(ReloadType.NO_AUTH)) {
                for (AuthProviderPair pair : config.auth.values()) {
                    reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".core"), pair.core);
                    reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
                    pair.close();
                }
            }
            if (type.equals(ReloadType.FULL)) {
                components.forEach((k, component) -> {
                    reconfigurableManager.unregisterObject("component.".concat(k), component);
                    if (component instanceof AutoCloseable autoCloseable) {
                        try {
                            autoCloseable.close();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e);
        }
        if (config.protectHandler != null) {
            reconfigurableManager.unregisterObject("protectHandler", config.protectHandler);
            config.protectHandler.close();
        }
        if (config.profileProvider != null) {
            reconfigurableManager.unregisterObject("profileProvider", config.profileProvider);
            config.profileProvider.close();
        }
        if (config.updatesProvider != null) {
            reconfigurableManager.unregisterObject("updatesProvider", config.updatesProvider);
            config.updatesProvider.close();
        }
    }

    public void checkCertificateExpired() {
        if (!config.sign.enabled) {
            return;
        }
        try {
            KeyStore keyStore = SignHelper.getStore(Paths.get(config.sign.keyStore), config.sign.keyStorePass, config.sign.keyStoreType);
            Instant date = SignHelper.getCertificateExpired(keyStore, config.sign.keyAlias);
            if (date == null) {
                logger.debug("The certificate will expire at unlimited");
            } else if (date.minus(Duration.ofDays(30)).isBefore(Instant.now())) {
                logger.warn("The certificate will expire at {}", date.toString());
            } else {
                logger.debug("The certificate will expire at {}", date.toString());
            }
        } catch (Throwable e) {
            logger.error("Can't get certificate expire date", e);
        }
    }
}
