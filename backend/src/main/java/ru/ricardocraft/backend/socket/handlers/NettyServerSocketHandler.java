package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.socket.LauncherNettyServer;

import java.net.InetSocketAddress;

@Component
public final class NettyServerSocketHandler implements Runnable, AutoCloseable {

    private transient final LaunchServerDirectories directories;
    private transient final LaunchServerRuntimeConfig runtimeConfig;
    private transient final LaunchServerConfig config;
    private transient final AuthManager authManager;
    private transient final AuthHookManager authHookManager;
    private transient final UpdatesManager updatesManager;
    private transient final KeyAgreementManager keyAgreementManager;
    private transient final JARLauncherBinary launcherBinary;
    private transient final EXELauncherBinary exeLauncherBinary;
    private transient final FeaturesManager featuresManager;

    private transient final Logger logger = LogManager.getLogger();
    public LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(LaunchServerDirectories directories,
                                    LaunchServerRuntimeConfig runtimeConfig,
                                    LaunchServerConfig config,
                                    AuthManager authManager,
                                    AuthHookManager authHookManager,
                                    UpdatesManager updatesManager,
                                    KeyAgreementManager keyAgreementManager,
                                    JARLauncherBinary launcherBinary,
                                    EXELauncherBinary exeLauncherBinary,
                                    FeaturesManager featuresManager) {
        this.directories = directories;
        this.config = config;
        this.runtimeConfig = runtimeConfig;
        this.authManager = authManager;
        this.authHookManager = authHookManager;
        this.updatesManager = updatesManager;
        this.keyAgreementManager = keyAgreementManager;
        this.launcherBinary = launcherBinary;
        this.exeLauncherBinary = exeLauncherBinary;
        this.featuresManager = featuresManager;
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
        nettyServer = new LauncherNettyServer(directories,
                runtimeConfig,
                config,
                authManager,
                authHookManager,
                updatesManager,
                keyAgreementManager,
                launcherBinary,
                exeLauncherBinary,
                featuresManager);
        for (LaunchServerConfig.NettyBindAddress address : config.netty.binds) {
            nettyServer.bind(new InetSocketAddress(address.address, address.port));
        }
    }
}
