package ru.ricardocraft.backend.command.updates.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;

@Component
public final class SyncProfilesCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    private transient final LaunchServerConfig config;
    private transient final ProfileProvider profileProvider;
    private transient final NettyServerSocketHandler nettyServerSocketHandler;

    public SyncProfilesCommand(LaunchServerConfig config,
                               ProfileProvider profileProvider,
                               NettyServerSocketHandler nettyServerSocketHandler) {
        super();
        this.config = config;
        this.profileProvider = profileProvider;
        this.nettyServerSocketHandler = nettyServerSocketHandler;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Resync profiles dir";
    }

    @Override
    public void invoke(String... args) throws IOException {
        profileProvider.syncProfilesDir(config, nettyServerSocketHandler);
        logger.info("Profiles successfully resynced");
    }
}
