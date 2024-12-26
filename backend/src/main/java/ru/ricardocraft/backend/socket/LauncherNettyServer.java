package ru.ricardocraft.backend.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.socket.handlers.NettyIpForwardHandler;
import ru.ricardocraft.backend.socket.handlers.NettyWebAPIHandler;
import ru.ricardocraft.backend.socket.handlers.fileserver.FileServerHandler;
import ru.ricardocraft.backend.socket.servlet.RemoteControlWebServlet;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class LauncherNettyServer implements AutoCloseable {

    private final Logger logger = LogManager.getLogger(LauncherNettyServer.class);

    private static final String WEBSOCKET_PATH = "/api";

    public final ServerBootstrap serverBootstrap;
    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final CommandHandler commandHandler;

    public LauncherNettyServer(LaunchServerProperties config,
                               DirectoriesManager directoriesManager,
                               NettyProperties nettyProperties,
                               CommandHandler commandHandler,
                               JacksonManager jacksonManager) {
        this.commandHandler = commandHandler;

        NettyObjectFactory.setUsingEpoll(nettyProperties.getPerformance().getUsingEpoll());
        if (nettyProperties.getPerformance().getUsingEpoll()) {
            logger.debug("Netty: Epoll enabled");
        }
        if (nettyProperties.getPerformance().getUsingEpoll() && !Epoll.isAvailable()) {
            logger.error("Epoll is not available: (netty,perfomance.usingEpoll configured wrongly)", Epoll.unavailabilityCause());
        }

        bossGroup = NettyObjectFactory.newEventLoopGroup(nettyProperties.getPerformance().getBossThread(), "LauncherNettyServer.bossGroup");
        workerGroup = NettyObjectFactory.newEventLoopGroup(nettyProperties.getPerformance().getWorkerThread(), "LauncherNettyServer.workerGroup");
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channelFactory(NettyObjectFactory.getServerSocketChannelFactory())
                .handler(new LoggingHandler(nettyProperties.getLogLevel()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(@NotNull SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        NettyConnectContext context = new NettyConnectContext();
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("http-codec-compressor", new HttpObjectAggregator(nettyProperties.getPerformance().getMaxWebSocketRequestBytes()));
                        if (nettyProperties.getIpForwarding()) // default false
                            pipeline.addLast("forward-http", new NettyIpForwardHandler(context));
                        pipeline.addLast("websock-comp", new WebSocketServerCompressionHandler());
                        pipeline.addLast("websock-codec", new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true, nettyProperties.getPerformance().getMaxWebSocketRequestBytes()));
                        if (!nettyProperties.getDisableWebApiInterface()) // default false
                            pipeline.addLast("webapi", new NettyWebAPIHandler(context));
                        if (nettyProperties.getFileServerEnabled()) // default true
                            pipeline.addLast("fileserver", new FileServerHandler(directoriesManager.getUpdatesDir(), true, nettyProperties.getShowHiddenFiles()));
                    }
                });

        NettyWebAPIHandler.addNewServlet("remotecontrol/command", new RemoteControlWebServlet(config, commandHandler, jacksonManager));
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
