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
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.components.AuthLimiterComponent;
import ru.ricardocraft.backend.components.ProGuardComponent;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.manangers.ReconfigurableManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Component
public final class LaunchServer implements Runnable, AutoCloseable {

    private final Logger logger = LogManager.getLogger();

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final LaunchServerProperties properties;

    private final AuthProviders authProviders;
    private final ProfileProvider profileProvider;
    private final UpdatesProvider updatesProvider;

    private final CommandHandler commandHandler;
    private final ProtectHandler protectHandler;
    private final NettyServerSocketHandler nettyServerSocketHandler;

    private final AuthLimiterComponent authLimiterComponent;
    private final ProGuardComponent proGuardComponent;

    private final ReconfigurableManager reconfigurableManager;

    @Autowired
    public LaunchServer(LaunchServerProperties properties,

                        AuthProviders authProviders,
                        ProfileProvider profileProvider,
                        UpdatesProvider updatesProvider,

                        CommandHandler commandHandler,
                        ProtectHandler protectHandler,
                        NettyServerSocketHandler nettyServerSocketHandler,

                        ReconfigurableManager reconfigurableManager,

                        AuthLimiterComponent authLimiterComponent,
                        ProGuardComponent proGuardComponent) throws IOException {

        this.properties = properties;

        this.authProviders = authProviders;
        this.profileProvider = profileProvider;
        this.updatesProvider = updatesProvider;

        this.commandHandler = commandHandler;
        this.protectHandler = protectHandler;
        this.nettyServerSocketHandler = nettyServerSocketHandler;

        this.authLimiterComponent = authLimiterComponent;
        this.proGuardComponent = proGuardComponent;

        this.reconfigurableManager = reconfigurableManager;

        reconfigurableManager.registerObject("component.authLimiter", authLimiterComponent);
        reconfigurableManager.registerObject("component.proguard", proGuardComponent);

        reconfigurableManager.registerObject("protectHandler", protectHandler);
        reconfigurableManager.registerObject("profileProvider", profileProvider);
        reconfigurableManager.registerObject("updatesProvider", updatesProvider);

        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".core"), pair.core);
            reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
        }
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
                profileProvider.syncProfilesDir();
                // Sync updates dir
                updatesProvider.syncInitially();
            } catch (IOException e) {
                logger.error("Updates/Profiles not synced", e);
            }
        }).start();

        if (properties.getNetty() != null) {
            nettyServerSocketHandler.close();
            CommonHelper.newThread("Netty Server Socket Thread", false, nettyServerSocketHandler).start();
        }

        logger.info("LaunchServer started");
    }

    public void close() throws Exception {
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

        logger.info("LaunchServer stopped");
    }
}
