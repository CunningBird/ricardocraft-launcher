package ru.ricardocraft.backend;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.helper.CommonHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.security.Security;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Component
public final class LaunchServer implements Runnable, AutoCloseable {

    private final Logger logger = LogManager.getLogger(LaunchServer.class);

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final ProfileProvider profileProvider;
    private final UpdatesProvider updatesProvider;
    private final CommandHandler commandHandler;
    private final NettyServerSocketHandler nettyServerSocketHandler;

    @Autowired
    public LaunchServer(ProfileProvider profileProvider,
                        UpdatesProvider updatesProvider,
                        CommandHandler commandHandler,
                        NettyServerSocketHandler nettyServerSocketHandler) throws IOException {
        this.profileProvider = profileProvider;
        this.updatesProvider = updatesProvider;
        this.commandHandler = commandHandler;
        this.nettyServerSocketHandler = nettyServerSocketHandler;
    }

    @PostConstruct
    public void init() {
        JVMHelper.verifySystemProperties(LaunchServer.class, false);
        Security.addProvider(new BouncyCastleProvider());
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

        nettyServerSocketHandler.close();
        CommonHelper.newThread("Netty Server Socket Thread", false, nettyServerSocketHandler).start();

        logger.info("LaunchServer started");
    }

    public void close() throws Exception {
        logger.info("Close server socket");
        nettyServerSocketHandler.close();
        logger.info("LaunchServer stopped");
    }
}
