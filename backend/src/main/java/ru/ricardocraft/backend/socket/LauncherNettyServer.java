package ru.ricardocraft.backend.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.socket.handlers.fileserver.FileServerHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LauncherNettyServer implements AutoCloseable {

    public final ServerBootstrap serverBootstrap;
    public final EventLoopGroup bossGroup;
    public final EventLoopGroup workerGroup;
    public final CommandHandler commandHandler;

    public LauncherNettyServer(DirectoriesManager directoriesManager,
                               NettyProperties nettyProperties,
                               CommandHandler commandHandler) {
        this.commandHandler = commandHandler;

        NettyObjectFactory.setUsingEpoll(nettyProperties.getPerformance().getUsingEpoll());
        if (nettyProperties.getPerformance().getUsingEpoll()) {
            log.debug("Netty: Epoll enabled");
        }
        if (nettyProperties.getPerformance().getUsingEpoll() && !Epoll.isAvailable()) {
            log.error("Epoll is not available: (netty,perfomance.usingEpoll configured wrongly)", Epoll.unavailabilityCause());
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
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("http-codec-compressor", new HttpObjectAggregator(nettyProperties.getPerformance().getMaxWebSocketRequestBytes()));
                        if (nettyProperties.getFileServerEnabled()) // default true
                            pipeline.addLast("fileserver", new FileServerHandler(directoriesManager.getUpdatesDir(), true, nettyProperties.getShowHiddenFiles()));
                    }
                });
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
