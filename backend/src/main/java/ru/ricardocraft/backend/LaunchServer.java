package ru.ricardocraft.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.command.CommandHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The main LaunchServer class. Contains links to all necessary objects
 * Not a singleton
 */
@Slf4j
@Component
public final class LaunchServer implements Runnable, AutoCloseable {

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final CommandHandler commandHandler;

    @Autowired
    public LaunchServer(CommandHandler commandHandler) throws IOException {
        this.commandHandler = commandHandler;
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

        log.info("LaunchServer started");
    }

    public void close() throws Exception {
        log.info("Close server socket");
        log.info("LaunchServer stopped");
    }

    private Thread newThread(String name, boolean daemon, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) thread.setName(name);
        return thread;
    }
}
