package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.socket.LauncherNettyServer;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.net.InetSocketAddress;

@Component
public final class NettyServerSocketHandler implements Runnable, AutoCloseable {

    private transient final Logger logger = LogManager.getLogger(NettyServerSocketHandler.class);

    private transient final LaunchServerConfig config;
    private transient final LaunchServerDirectories directories;
    private transient final WebSocketService service;
    private transient final CommandHandler commandHandler;
    private transient final JacksonManager jacksonManager;

    private transient LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(LaunchServerConfig config,
                                    LaunchServerDirectories directories,
                                    WebSocketService service,
                                    CommandHandler commandHandler,
                                    JacksonManager jacksonManager) {
        this.config = config;
        this.directories = directories;
        this.service = service;
        this.commandHandler = commandHandler;
        this.jacksonManager = jacksonManager;
    }

    @Override
    public void close() {
        if (nettyServer == null) return;
        nettyServer.close();
        nettyServer.service.channels.close();
    }

    @Override
    public void run() {
        logger.info("Starting netty server socket thread");
        nettyServer = new LauncherNettyServer(config, directories, service, commandHandler, jacksonManager);
        for (LaunchServerConfig.NettyBindAddress address : config.netty.binds) {
            nettyServer.bind(new InetSocketAddress(address.address, address.port));
        }
    }
}
