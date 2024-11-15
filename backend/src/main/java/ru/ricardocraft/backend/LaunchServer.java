package ru.ricardocraft.backend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.components.AuthLimiterComponent;
import ru.ricardocraft.backend.components.ProGuardComponent;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.properties.LauncherEnvironment;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Component
public final class LaunchServer implements Runnable, AutoCloseable {

    private final Logger logger = LogManager.getLogger();

    public final AtomicBoolean started = new AtomicBoolean(false);
    public final ScheduledExecutorService service;

    public LaunchServerConfig config;
    public final LaunchServerRuntimeConfig runtime;
    public final CommandHandler commandHandler;

    private final ProtectHandler protectHandler;
    private final ProfileProvider profileProvider;
    private final UpdatesProvider updatesProvider;

    private final AuthProviders authProviders;
    private final AuthLimiterComponent authLimiterComponent;
    private final ProGuardComponent proGuardComponent;

    public final LaunchServerConfigManager launchServerConfigManager;
    public final ReconfigurableManager reconfigurableManager;

    public final NettyServerSocketHandler nettyServerSocketHandler;

    @Autowired
    public LaunchServer(ProtectHandler protectHandler,
                        ProfileProvider profileProvider,
                        UpdatesProvider updatesProvider,

                        AuthProviders authProviders,
                        LaunchServerConfig config,
                        LaunchServerRuntimeConfig runtimeConfig,
                        LaunchServerConfigManager launchServerConfigManager,
                        CommandHandler commandHandler,
                        ReconfigurableManager reconfigurableManager,
                        MirrorManager mirrorManager,
                        NettyServerSocketHandler nettyServerSocketHandler,

                        AuthLimiterComponent authLimiterComponent,
                        ProGuardComponent proGuardComponent) throws IOException {

        this.service = Executors.newScheduledThreadPool(config.netty.performance.schedulerThread);

        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
        this.updatesProvider = updatesProvider;

        this.authLimiterComponent = authLimiterComponent;
        this.proGuardComponent = proGuardComponent;

        this.config = config;
        this.runtime = runtimeConfig;
        this.commandHandler = commandHandler;

        this.authProviders = authProviders;
        this.launchServerConfigManager = launchServerConfigManager;
        this.reconfigurableManager = reconfigurableManager;

        this.nettyServerSocketHandler = nettyServerSocketHandler;

        Launcher.applyLauncherEnv(LauncherEnvironment.DEV);

        reconfigurableManager.registerObject("component.authLimiter", authLimiterComponent);
        reconfigurableManager.registerObject("component.proguard", proGuardComponent);

        reconfigurableManager.registerObject("protectHandler", protectHandler);
        reconfigurableManager.registerObject("profileProvider", profileProvider);
        reconfigurableManager.registerObject("updatesProvider", updatesProvider);

        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".core"), pair.core);
            reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
        }

        Arrays.stream(config.mirrors).forEach(mirrorManager::addMirror);

        reconfigurableManager.registerObject("launchServer", this);
    }

    @Override
    public void run() {
        if (started.getAndSet(true))
            throw new IllegalStateException("LaunchServer has been already started");

        // Add shutdown hook, then start LaunchServer
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
                profileProvider.syncProfilesDir(config, nettyServerSocketHandler);
                // Sync updates dir
                updatesProvider.syncInitially();
            } catch (IOException e) {
                logger.error("Updates/Profiles not synced", e);
            }
        }).start();

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

        try {
            for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
                reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".core"), pair.core);
                reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
                pair.close();
            }
        } catch (Exception e) {
            logger.error(e);
        }

        reconfigurableManager.unregisterObject("component.authLimiter", authLimiterComponent);
        reconfigurableManager.unregisterObject("component.proguard", proGuardComponent);
        authLimiterComponent.close();
        proGuardComponent.close();

        reconfigurableManager.unregisterObject("protectHandler", protectHandler);
        reconfigurableManager.unregisterObject("profileProvider", profileProvider);
        reconfigurableManager.unregisterObject("updatesProvider", updatesProvider);

        logger.info("Save LaunchServer runtime config");
        launchServerConfigManager.writeRuntimeConfig(runtime);

        logger.info("LaunchServer stopped");
    }
}
