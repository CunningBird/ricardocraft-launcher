package ru.ricardocraft.backend.socket.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
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

    private transient final LaunchServerConfig config;
    private transient final LaunchServerRuntimeConfig runtimeConfig;
    private transient final AuthProviders authProviders;
    private transient final AuthManager authManager;

    private transient final AuthHookManager authHookManager;
    private transient final UpdatesManager updatesManager;
    private transient final KeyAgreementManager keyAgreementManager;
    private transient final JARLauncherBinary launcherBinary;

    private transient final EXELauncherBinary exeLauncherBinary;
    private transient final FeaturesManager featuresManager;
    private transient final ProtectHandler protectHandler;
    private transient final ProfileProvider profileProvider;

    private transient final Logger logger = LogManager.getLogger();
    public LauncherNettyServer nettyServer;

    @Autowired
    public NettyServerSocketHandler(LaunchServerDirectories directories,
                                    LaunchServerRuntimeConfig runtimeConfig,
                                    LaunchServerConfig config,
                                    AuthProviders authProviders,
                                    AuthManager authManager,
                                    AuthHookManager authHookManager,
                                    UpdatesManager updatesManager,
                                    KeyAgreementManager keyAgreementManager,
                                    JARLauncherBinary launcherBinary,
                                    EXELauncherBinary exeLauncherBinary,
                                    FeaturesManager featuresManager,
                                    ProtectHandler protectHandler,
                                    ProfileProvider profileProvider) {
        this.directories = directories;

        this.config = config;
        this.runtimeConfig = runtimeConfig;
        this.authProviders = authProviders;
        this.authManager = authManager;

        this.authHookManager = authHookManager;
        this.updatesManager = updatesManager;
        this.keyAgreementManager = keyAgreementManager;
        this.launcherBinary = launcherBinary;

        this.exeLauncherBinary = exeLauncherBinary;
        this.featuresManager = featuresManager;
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
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
                config,
                runtimeConfig,
                authProviders,
                authManager,
                authHookManager,
                updatesManager,
                keyAgreementManager,
                launcherBinary,
                exeLauncherBinary,
                featuresManager,
                protectHandler,
                profileProvider);
        for (LaunchServerConfig.NettyBindAddress address : config.netty.binds) {
            nettyServer.bind(new InetSocketAddress(address.address, address.port));
        }
    }
}
