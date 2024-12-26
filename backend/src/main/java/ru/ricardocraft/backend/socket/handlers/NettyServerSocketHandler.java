package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.properties.netty.NettyBindAddressProperties;
import ru.ricardocraft.backend.socket.LauncherNettyServer;

import java.net.InetSocketAddress;

@Component
public final class NettyServerSocketHandler implements Runnable, AutoCloseable {

    private transient final Logger logger = LogManager.getLogger(NettyServerSocketHandler.class);

    private transient final DirectoriesManager directoriesManager;
    private transient final NettyProperties nettyProperties;
    private transient final CommandHandler commandHandler;

    private transient LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(DirectoriesManager directoriesManager,
                                    NettyProperties nettyProperties,
                                    CommandHandler commandHandler) {
        this.directoriesManager = directoriesManager;
        this.nettyProperties = nettyProperties;
        this.commandHandler = commandHandler;
    }

    @Override
    public void close() {
        if (nettyServer == null) return;
        nettyServer.close();
    }

    @Override
    public void run() {
        logger.info("Starting netty server socket thread");
        nettyServer = new LauncherNettyServer(directoriesManager, nettyProperties, commandHandler);
        for (NettyBindAddressProperties address : nettyProperties.getBinds()) {
            nettyServer.bind(new InetSocketAddress(address.getAddress(), address.getPort()));
        }
    }
}
