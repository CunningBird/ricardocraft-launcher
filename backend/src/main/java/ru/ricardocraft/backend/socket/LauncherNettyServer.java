package ru.ricardocraft.backend.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;
import ru.ricardocraft.backend.socket.handlers.NettyIpForwardHandler;
import ru.ricardocraft.backend.socket.handlers.NettyWebAPIHandler;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;
import ru.ricardocraft.backend.socket.handlers.fileserver.FileServerHandler;
import ru.ricardocraft.backend.utils.BiHookSet;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class LauncherNettyServer implements AutoCloseable {
    private static final String WEBSOCKET_PATH = "/api";
    public final ServerBootstrap serverBootstrap;
    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final WebSocketService service;
    public final BiHookSet<NettyConnectContext, SocketChannel> pipelineHook = new BiHookSet<>();

    public LauncherNettyServer(LaunchServerDirectories directories,
                               LaunchServerRuntimeConfig runtimeConfig,
                               LaunchServerConfig config,
                               AuthManager authManager,
                               AuthHookManager authHookManager,
                               UpdatesManager updatesManager,
                               KeyAgreementManager keyAgreementManager,
                               JARLauncherBinary launcherBinary,
                               EXELauncherBinary exeLauncherBinary,
                               FeaturesManager featuresManager) {

        LaunchServerConfig.NettyConfig nettyConfig = config.netty;
        NettyObjectFactory.setUsingEpoll(nettyConfig.performance.usingEpoll);
        Logger logger = LogManager.getLogger();
        if (nettyConfig.performance.usingEpoll) {
            logger.debug("Netty: Epoll enabled");
        }
        if (nettyConfig.performance.usingEpoll && !Epoll.isAvailable()) {
            logger.error("Epoll is not available: (netty,perfomance.usingEpoll configured wrongly)", Epoll.unavailabilityCause());
        }
        bossGroup = NettyObjectFactory.newEventLoopGroup(nettyConfig.performance.bossThread, "LauncherNettyServer.bossGroup");
        workerGroup = NettyObjectFactory.newEventLoopGroup(nettyConfig.performance.workerThread, "LauncherNettyServer.workerGroup");
        serverBootstrap = new ServerBootstrap();
        service = new WebSocketService(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                runtimeConfig,
                config,
                authManager,
                authHookManager,
                updatesManager,
                keyAgreementManager,
                launcherBinary,
                exeLauncherBinary,
                featuresManager,
                Integer.parseInt(System.getProperty("launchserver.shardId", "0")));
        serverBootstrap.group(bossGroup, workerGroup)
                .channelFactory(NettyObjectFactory.getServerSocketChannelFactory())
                .handler(new LoggingHandler(nettyConfig.logLevel))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(@NotNull SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        NettyConnectContext context = new NettyConnectContext();
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("http-codec-compressor", new HttpObjectAggregator(config.netty.performance.maxWebSocketRequestBytes));
                        if (config.netty.ipForwarding) // default false
                            pipeline.addLast("forward-http", new NettyIpForwardHandler(context));
                        pipeline.addLast("websock-comp", new WebSocketServerCompressionHandler());
                        pipeline.addLast("websock-codec", new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, config.netty.performance.maxWebSocketRequestBytes));
                        if (!config.netty.disableWebApiInterface) // default false
                            pipeline.addLast("webapi", new NettyWebAPIHandler(context));
                        if (config.netty.fileServerEnabled) // default true
                            pipeline.addLast("fileserver", new FileServerHandler(directories.updatesDir, true, nettyConfig.showHiddenFiles));
                        pipeline.addLast("launchserver", new WebSocketFrameHandler(context, service));
                        pipelineHook.hook(context, ch);
                    }
                });

//        NettyWebAPIHandler.addNewSeverlet("remotecontrol/command", new RemoteControlWebServlet(config));
    }

    public void bind(InetSocketAddress address) {
        serverBootstrap.bind(address);
    }

    @Override
    public void close() {
        workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
        bossGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
    }
}
