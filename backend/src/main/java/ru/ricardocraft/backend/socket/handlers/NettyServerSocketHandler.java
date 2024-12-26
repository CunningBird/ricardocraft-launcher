package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.properties.netty.NettyBindAddressProperties;
import ru.ricardocraft.backend.socket.LauncherNettyServer;

import java.net.InetSocketAddress;

@Component
public final class NettyServerSocketHandler implements Runnable, AutoCloseable {

    private transient final Logger logger = LogManager.getLogger(NettyServerSocketHandler.class);

    private transient final LaunchServerProperties config;
    private transient final DirectoriesManager directoriesManager;
    private transient final NettyProperties nettyProperties;
    private transient final CommandHandler commandHandler;
    private transient final JacksonManager jacksonManager;

    private transient LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(LaunchServerProperties config,
                                    DirectoriesManager directoriesManager,
                                    NettyProperties nettyProperties,
                                    CommandHandler commandHandler,
                                    JacksonManager jacksonManager) {
        this.config = config;
        this.directoriesManager = directoriesManager;
        this.nettyProperties = nettyProperties;
        this.commandHandler = commandHandler;
        this.jacksonManager = jacksonManager;
    }

    @Override
    public void close() {
        if (nettyServer == null) return;
        nettyServer.close();
    }

    @Override
    public void run() {
        logger.info("Starting netty server socket thread");
        nettyServer = new LauncherNettyServer(config, directoriesManager, nettyProperties, commandHandler, jacksonManager);
        for (NettyBindAddressProperties address : nettyProperties.getBinds()) {
            nettyServer.bind(new InetSocketAddress(address.getAddress(), address.getPort()));
        }
    }
}
