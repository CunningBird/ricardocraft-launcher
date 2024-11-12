package ru.ricardocraft.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.RejectAuthCoreProvider;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.binary.LauncherBinary;
import ru.ricardocraft.backend.command.GenerateCertificateCommand;
import ru.ricardocraft.backend.command.OSSLSignEXECommand;
import ru.ricardocraft.backend.command.basic.*;
import ru.ricardocraft.backend.command.updates.DownloadAssetCommand;
import ru.ricardocraft.backend.command.updates.DownloadClientCommand;
import ru.ricardocraft.backend.command.updates.IndexAssetCommand;
import ru.ricardocraft.backend.command.updates.UnindexAssetCommand;
import ru.ricardocraft.backend.command.mirror.*;
import ru.ricardocraft.backend.command.updates.profile.ProfilesCommand;
import ru.ricardocraft.backend.command.remotecontrol.RemoteControlCommand;
import ru.ricardocraft.backend.command.service.*;
import ru.ricardocraft.backend.command.updates.sync.SyncCommand;
import ru.ricardocraft.backend.command.tools.SignDirCommand;
import ru.ricardocraft.backend.command.tools.SignJarCommand;
import ru.ricardocraft.backend.command.unsafe.*;
import ru.ricardocraft.backend.command.utls.*;
import ru.ricardocraft.backend.manangers.LaunchServerConfigManager;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.helper.SignHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.manangers.AuthHookManager;
import ru.ricardocraft.backend.properties.*;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
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
 * Not a singleton
 */
@Component
public final class LaunchServer implements Runnable, AutoCloseable, Reconfigurable {

    private final Logger logger = LogManager.getLogger();

    public final AtomicBoolean started = new AtomicBoolean(false);
    public final ScheduledExecutorService service;
    public final int shardId;

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

    @Autowired
    public LaunchServer(LaunchServerProperties launchServerProperties,
                        LaunchServerDirectories directories,
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
                        EXELauncherBinary launcherEXEBinary) throws IOException {

        this.service = Executors.newScheduledThreadPool(config.netty.performance.schedulerThread);
        this.shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));

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

        registerCommands(commandHandler, this);

        this.launcherBinary = launcherBinary;
        this.launcherEXEBinary = launcherEXEBinary;
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

        config.setLaunchServer(this);
        config.init(ReloadType.FULL);

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
                    syncProfilesDir();

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
        config.close(ReloadType.FULL);

        logger.info("Save LaunchServer runtime config");
        launchServerConfigManager.writeRuntimeConfig(runtime);
        // Print last message before death :(
        logger.info("LaunchServer stopped");
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
        if (!type.equals(ReloadType.NO_AUTH)) {
            nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((channel, wsHandler) -> {
                Client client = wsHandler.getClient();
                if (client.auth != null) {
                    client.auth = config.getAuthProviderPair(client.auth_id);
                }
            });
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

    public void buildLauncherBinaries() throws IOException {
        launcherBinary.build();
        launcherEXEBinary.build();
    }

    public void syncLauncherBinaries() throws IOException {
        logger.info("Syncing launcher binaries");

        // Syncing launcher binary
        logger.info("Syncing launcher binary file");
        if (!launcherBinary.sync()) logger.warn("Missing launcher binary file");

        // Syncing launcher EXE binary
        logger.info("Syncing launcher EXE binary file");
        if (!launcherEXEBinary.sync()) logger.warn("Missing launcher EXE binary file");
    }

    public void syncProfilesDir() throws IOException {
        logger.info("Syncing profiles dir");
        config.profileProvider.sync();
        if (config.netty.sendProfileUpdatesEvent) {
            sendUpdateProfilesEvent();
        }
    }

    public void syncUpdatesDir(Collection<String> dirs) throws IOException {
        updatesManager.syncUpdatesDir(dirs);
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

    private void registerCommands(CommandHandler handler, LaunchServer server) {
        BaseCommandCategory basic = new BaseCommandCategory();
        // Register basic commands
        basic.registerCommand("build", new BuildCommand(server));
        basic.registerCommand("clear", new ClearCommand(handler));
        basic.registerCommand("debug", new DebugCommand(server));
        basic.registerCommand("gc", new GCCommand());
        basic.registerCommand("help", new HelpCommand(handler));
        basic.registerCommand("stop", new StopCommand(server));
        basic.registerCommand("version", new VersionCommand(server));
        CommandHandler.Category basicCategory = new CommandHandler.Category(basic, "basic", "Base LaunchServer commands");
        handler.registerCategory(basicCategory);

        // Register sync commands
        BaseCommandCategory updates = new BaseCommandCategory();
        updates.registerCommand("profile", new ProfilesCommand(server));
        updates.registerCommand("sync", new SyncCommand(server));
        updates.registerCommand("indexAsset", new IndexAssetCommand(server));
        updates.registerCommand("unindexAsset", new UnindexAssetCommand(server));
        updates.registerCommand("downloadAsset", new DownloadAssetCommand(server));
        updates.registerCommand("downloadClient", new DownloadClientCommand(server));
        CommandHandler.Category updatesCategory = new CommandHandler.Category(updates, "updates", "Update and Sync Management");
        handler.registerCategory(updatesCategory);

        //Register service commands
        BaseCommandCategory service = new BaseCommandCategory();
        service.registerCommand("config", new ConfigCommand(server));
        service.registerCommand("serverStatus", new ServerStatusCommand(server));
        service.registerCommand("notify", new NotifyCommand(server));
        service.registerCommand("component", new ComponentCommand(server));
        service.registerCommand("clients", new ClientsCommand(server));
        service.registerCommand("securitycheck", new SecurityCheckCommand(server));
        service.registerCommand("token", new TokenCommand(server));
        CommandHandler.Category serviceCategory = new CommandHandler.Category(service, "service", "Managing LaunchServer Components");
        handler.registerCategory(serviceCategory);

        //Register tools commands
        BaseCommandCategory tools = new BaseCommandCategory();
        tools.registerCommand("signJar", new SignJarCommand(server));
        tools.registerCommand("signDir", new SignDirCommand(server));
        CommandHandler.Category toolsCategory = new CommandHandler.Category(tools, "tools", "Other tools");
        handler.registerCategory(toolsCategory);

        BaseCommandCategory unsafe = new BaseCommandCategory();
        unsafe.registerCommand("loadJar", new LoadJarCommand(server));
        unsafe.registerCommand("registerComponent", new RegisterComponentCommand(server));
        unsafe.registerCommand("sendAuth", new SendAuthCommand(server));
        unsafe.registerCommand("patcher", new PatcherCommand(server));
        unsafe.registerCommand("cipherList", new CipherListCommand(server));
        CommandHandler.Category unsafeCategory = new CommandHandler.Category(unsafe, "Unsafe");
        handler.registerCategory(unsafeCategory);

        BaseCommandCategory mirror = new BaseCommandCategory();
        mirror.registerCommand("curseforge", new CurseforgeCommand(server));
        mirror.registerCommand("installClient", new InstallClientCommand(server));
        mirror.registerCommand("installMods", new InstallModCommand(server));
        mirror.registerCommand("deduplibraries", new DeDupLibrariesCommand(server));
        mirror.registerCommand("launchInstaller", new LaunchInstallerCommand(server));
        mirror.registerCommand("lwjgldownload", new LwjglDownloadCommand(server));
        mirror.registerCommand("patchauthlib", new PatchAuthlibCommand(server));
        mirror.registerCommand("applyworkspace", new ApplyWorkspaceCommand(server));
        mirror.registerCommand("workspace", new WorkspaceCommand(server));
        CommandHandler.Category category = new CommandHandler.Category(mirror, "mirror");
        handler.registerCategory(category);

        handler.registerCommand("generatecertificate", new GenerateCertificateCommand(server));
        handler.registerCommand("osslsignexe", new OSSLSignEXECommand(server));
        handler.registerCommand("remotecontrol", new RemoteControlCommand(server));
    }
}
