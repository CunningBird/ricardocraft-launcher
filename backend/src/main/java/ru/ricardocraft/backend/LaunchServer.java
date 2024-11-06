package ru.ricardocraft.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.RejectAuthCoreProvider;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.binary.LauncherBinary;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerEnv;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.config.LaunchServerConfigManager;
import ru.ricardocraft.backend.configuration.LaunchServerDirectories;
import ru.ricardocraft.backend.helper.SignHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.manangers.hook.AuthHookManager;
import ru.ricardocraft.backend.properties.ReloadType;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;
import ru.ricardocraft.backend.socket.response.auth.RestoreResponse;
import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.JVMHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singletron
 */
public final class LaunchServer implements Runnable, AutoCloseable, Reconfigurable {
    /**
     * Working folder path
     */
    public final Path dir;
    /**
     * Environment type (test / production)
     */
    public final LaunchServerEnv env;
    /**
     * The path to the folder with libraries for the launcher
     */
    public final Path launcherLibraries;
    /**
     * The path to the folder with compile-only libraries for the launcher
     */
    public final Path launcherLibrariesCompile;
    public final Path launcherPack;
    /**
     * The path to the folder with updates/webroot
     */
    @Deprecated
    public final Path updatesDir;

    // Constant paths
    /**
     * Save/Reload LaunchServer config
     */
    public final LaunchServerConfigManager launchServerConfigManager;
    /**
     * The path to the folder with profiles
     */
    public final Path tmpDir;
    public final Path librariesDir;
    /**
     * This object contains runtime configuration
     */
    public final LaunchServerRuntimeConfig runtime;
    /**
     * Pipeline for building JAR
     */
    public final JARLauncherBinary launcherBinary;
    /**
     * Pipeline for building EXE
     */
    public final LauncherBinary launcherEXEBinary;
    // Server config
    public final AuthHookManager authHookManager;
    // Launcher binary
    public final MirrorManager mirrorManager;
    public final AuthManager authManager;
    public final ReconfigurableManager reconfigurableManager;
    public final ConfigManager configManager;
    public final FeaturesManager featuresManager;
    public final KeyAgreementManager keyAgreementManager;
    public final UpdatesManager updatesManager;
    // HWID ban + anti-brutforce
    public final CertificateManager certificateManager;
    // Server
    public final CommandHandler commandHandler;
    public final NettyServerSocketHandler nettyServerSocketHandler;
    public final ScheduledExecutorService service;
    public final AtomicBoolean started = new AtomicBoolean(false);
    private final Logger logger = LogManager.getLogger();
    public final int shardId;
    public LaunchServerConfig config;

    public LaunchServer(LaunchServerDirectories directories, LaunchServerEnv env, LaunchServerConfig config, LaunchServerRuntimeConfig runtimeConfig, LaunchServerConfigManager launchServerConfigManager, KeyAgreementManager keyAgreementManager, CommandHandler commandHandler, CertificateManager certificateManager, int shardId) throws IOException {
        this.dir = directories.dir;
        this.tmpDir = directories.tmpDir;
        this.env = env;
        this.config = config;
        this.launchServerConfigManager = launchServerConfigManager;
        this.updatesDir = directories.updatesDir;
        this.keyAgreementManager = keyAgreementManager;
        this.commandHandler = commandHandler;
        this.runtime = runtimeConfig;
        this.certificateManager = certificateManager;
        this.service = Executors.newScheduledThreadPool(config.netty.performance.schedulerThread);
        launcherLibraries = directories.launcherLibrariesDir;
        launcherLibrariesCompile = directories.launcherLibrariesCompileDir;
        launcherPack = directories.launcherPackDir;
        librariesDir = directories.librariesDir;
        this.shardId = shardId;
        if(!Files.isDirectory(launcherPack)) {
            Files.createDirectories(launcherPack);
        }

        config.setLaunchServer(this);

        // Print keypair fingerprints

        runtime.verify();
        config.verify();

        // build hooks, anti-brutforce and other
        mirrorManager = new MirrorManager(this);
        reconfigurableManager = new ReconfigurableManager();
        authHookManager = new AuthHookManager();
        configManager = new ConfigManager();
        featuresManager = new FeaturesManager(this);
        authManager = new AuthManager(this);
        updatesManager = new UpdatesManager(this);
        RestoreResponse.registerProviders(this);

        config.init(ReloadType.FULL);
        registerObject("launchServer", this);

        ru.ricardocraft.backend.command.handler.CommandHandler.registerCommands(commandHandler, this);

        // Set launcher EXE binary
        launcherBinary = new JARLauncherBinary(this);
        launcherEXEBinary = binary();

        launcherBinary.init();
        launcherEXEBinary.init();
        syncLauncherBinaries();

        if (config.components != null) {
            logger.debug("Init components");
            config.components.forEach((k, v) -> {
                logger.debug("Init component {}", k);
                v.setComponentName(k);
                v.init(this);
            });
            logger.debug("Init components successful");
        }

        nettyServerSocketHandler = new NettyServerSocketHandler(this);
        if(config.sign.checkCertificateExpired) {
            checkCertificateExpired();
            service.scheduleAtFixedRate(this::checkCertificateExpired, 24, 24, TimeUnit.HOURS);
        }
    }

    public void reload(ReloadType type) throws Exception {
        config.close(type);
        Map<String, AuthProviderPair> pairs = null;
        if (type.equals(ReloadType.NO_AUTH)) {
            pairs = config.auth;
        }
        logger.info("Reading LaunchServer config file");
        config = launchServerConfigManager.readConfig();
        config.setLaunchServer(this);
        if (type.equals(ReloadType.NO_AUTH)) {
            config.auth = pairs;
        }
        config.verify();
        config.init(type);
        if (type.equals(ReloadType.FULL) && config.components != null) {
            logger.debug("Init components");
            config.components.forEach((k, v) -> {
                logger.debug("Init component {}", k);
                v.setComponentName(k);
                v.init(this);
            });
            logger.debug("Init components successful");
        }
        if(!type.equals(ReloadType.NO_AUTH)) {
            nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((channel, wsHandler) -> {
                Client client = wsHandler.getClient();
                if(client.auth != null) {
                    client.auth = config.getAuthProviderPair(client.auth_id);
                }
            });
        }
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
        LaunchServer instance = this;
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
                pair.core.init(instance, pair);
            }
        };
        commands.put("resetauth", resetauth);
        return commands;
    }

    public void checkCertificateExpired() {
        if(!config.sign.enabled) {
            return;
        }
        try {
            KeyStore keyStore = SignHelper.getStore(Paths.get(config.sign.keyStore), config.sign.keyStorePass, config.sign.keyStoreType);
            Instant date = SignHelper.getCertificateExpired(keyStore, config.sign.keyAlias);
            if(date == null) {
                logger.debug("The certificate will expire at unlimited");
            } else if(date.minus(Duration.ofDays(30)).isBefore(Instant.now())) {
                logger.warn("The certificate will expire at {}", date.toString());
            } else {
                logger.debug("The certificate will expire at {}", date.toString());
            }
        } catch (Throwable e) {
            logger.error("Can't get certificate expire date", e);
        }
    }

    private LauncherBinary binary() {
        return new EXELauncherBinary(this);
    }

    public void buildLauncherBinaries() throws IOException {
        launcherBinary.build();
        launcherEXEBinary.build();
    }

    public void close() throws Exception {
        service.shutdownNow();
        logger.info("Close server socket");
        nettyServerSocketHandler.close();
        // Close handlers & providers
        config.close(ReloadType.FULL);

        logger.info("Save LaunchServer runtime config");
        launchServerConfigManager.writeRuntimeConfig(runtime);
        // Print last message before death :(
        logger.info("LaunchServer stopped");
    }

    @Deprecated
    public Set<ClientProfile> getProfiles() {
        return config.profileProvider.getProfiles();
    }

    @Deprecated
    public void setProfiles(Set<ClientProfile> profilesList) {
        throw new UnsupportedOperationException();
    }

    public void rebindNettyServerSocket() {
        nettyServerSocketHandler.close();
        CommonHelper.newThread("Netty Server Socket Thread", false, nettyServerSocketHandler).start();
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
                    syncProfilesDir();

                    // Sync updates dir
                    config.updatesProvider.syncInitially();
                } catch (IOException e) {
                    logger.error("Updates/Profiles not synced", e);
                }
            }).start();
        }
        if (config.netty != null)
            rebindNettyServerSocket();

        logger.info("LaunchServer started");
    }

    public void syncLauncherBinaries() throws IOException {
        logger.info("Syncing launcher binaries");

        // Syncing launcher binary
        logger.info("Syncing launcher binary file");
        if (!launcherBinary.sync()) logger.warn("Missing launcher binary file");

        // Syncing launcher EXE binary
        logger.info("Syncing launcher EXE binary file");
        if (!launcherEXEBinary.sync())
            logger.warn("Missing launcher EXE binary file");

    }

    public void syncProfilesDir() throws IOException {
        logger.info("Syncing profiles dir");
        config.profileProvider.sync();
        if (config.netty.sendProfileUpdatesEvent) {
            sendUpdateProfilesEvent();
        }
    }

    private void sendUpdateProfilesEvent() {
        if (nettyServerSocketHandler == null || nettyServerSocketHandler.nettyServer == null || nettyServerSocketHandler.nettyServer.service == null) {
            return;
        }
        nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((ch, handler) -> {
            Client client = handler.getClient();
            if (client == null || !client.isAuth) {
                return;
            }
            ProfilesRequestEvent event = new ProfilesRequestEvent(config.profileProvider.getProfiles(client));
            event.requestUUID = RequestEvent.eventUUID;
            handler.service.sendObject(ch, event);
        });
    }

    public void syncUpdatesDir(Collection<String> dirs) throws IOException {
        updatesManager.syncUpdatesDir(dirs);
    }

    public void registerObject(String name, Object object) {
        if (object instanceof Reconfigurable) {
            reconfigurableManager.registerReconfigurable(name, (Reconfigurable) object);
        }
    }

    public void unregisterObject(String name, Object object) {
        if (object instanceof Reconfigurable) {
            reconfigurableManager.unregisterReconfigurable(name);
        }
    }
}
