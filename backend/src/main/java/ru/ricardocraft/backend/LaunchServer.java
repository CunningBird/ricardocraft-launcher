package ru.ricardocraft.backend;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.security.Security;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.ricardocraft.backend.base.helper.JVMHelper.LOADER;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Slf4j
@Component
public final class LaunchServer implements Runnable, AutoCloseable {

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final CommandHandler commandHandler;
    private final NettyServerSocketHandler nettyServerSocketHandler;

    @Autowired
    public LaunchServer(CommandHandler commandHandler, NettyServerSocketHandler nettyServerSocketHandler) throws IOException {
        this.commandHandler = commandHandler;
        this.nettyServerSocketHandler = nettyServerSocketHandler;
    }

    @Override
    public void run() {
        if (started.getAndSet(true))
            throw new IllegalStateException("LaunchServer has been already started");

        // Add shutdown hook, then start LaunchServer
        JVMHelper.RUNTIME.addShutdownHook(newThread(null, false, () -> {
            try {
                close();
            } catch (Exception e) {
                log.error("LaunchServer close error", e);
            }
        }));
        newThread("Command Thread", true, commandHandler).start();

//        nettyServerSocketHandler.close();
//        newThread("Netty Server Socket Thread", false, nettyServerSocketHandler).start();

        log.info("LaunchServer started");
    }

    public void close() throws Exception {
        log.info("Close server socket");
        nettyServerSocketHandler.close();
        log.info("LaunchServer stopped");
    }

    private Thread newThread(String name, boolean daemon, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) thread.setName(name);
        return thread;
    }
}
