package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.socket.LauncherNettyServer;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.net.InetSocketAddress;

@Component
public final class NettyServerSocketHandler implements Runnable, AutoCloseable {

    private transient final Logger logger = LogManager.getLogger();

    private transient final LaunchServerConfig config;
    private transient final LaunchServerDirectories directories;
    private transient final WebSocketService service;

    public LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(LaunchServerConfig config,
                                    LaunchServerDirectories directories,
                                    WebSocketService service) {
        this.config = config;
        this.directories = directories;
        this.service = service;
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
        nettyServer = new LauncherNettyServer(config, directories, service);
        for (LaunchServerConfig.NettyBindAddress address : config.netty.binds) {
            nettyServer.bind(new InetSocketAddress(address.address, address.port));
        }
    }
}
