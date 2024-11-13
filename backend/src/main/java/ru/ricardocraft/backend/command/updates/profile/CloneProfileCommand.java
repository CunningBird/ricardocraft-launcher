package ru.ricardocraft.backend.command.updates.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.ClientProfileBuilder;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class CloneProfileCommand extends Command {
    private final transient Logger logger = LogManager.getLogger(CloneProfileCommand.class);

    private transient final LaunchServerConfig config;
    private transient final LaunchServerDirectories directories;
    private transient final NettyServerSocketHandler nettyServerSocketHandler;
    private transient final UpdatesManager updatesManager;

    @Autowired
    public CloneProfileCommand(LaunchServerConfig config,
                               LaunchServerDirectories directories,
                               NettyServerSocketHandler nettyServerSocketHandler,
                               UpdatesManager updatesManager) {
        super();
        this.config = config;
        this.directories = directories;
        this.nettyServerSocketHandler = nettyServerSocketHandler;
        this.updatesManager = updatesManager;
    }

    @Override
    public String getArgsDescription() {
        return "[profile title/uuid] [new profile title]";
    }

    @Override
    public String getUsageDescription() {
        return "clone profile and profile dir";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        ClientProfile profile;
        try {
            UUID uuid = UUID.fromString(args[0]);
            profile = config.profileProvider.getProfile(uuid);
        } catch (IllegalArgumentException ex) {
            profile = config.profileProvider.getProfile(args[0]);
        }
        var builder = new ClientProfileBuilder(profile);
        builder.setTitle(args[1]);
        builder.setUuid(UUID.randomUUID());
        if (profile.getServers().size() == 1) {
            profile.getServers().getFirst().name = args[1];
        }
        logger.info("Copy {} to {}", profile.getDir(), args[1]);
        var src = directories.updatesDir.resolve(profile.getDir());
        var dest = directories.updatesDir.resolve(args[1]);
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> {
                try {
                    IOHelper.copy(source, dest.resolve(src.relativize(source)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        builder.setDir(args[1]);
        profile = builder.createClientProfile();
        config.profileProvider.addProfile(profile);
        logger.info("Profile {} cloned from {}", args[1], args[0]);
        config.profileProvider.syncProfilesDir(config, nettyServerSocketHandler);
        updatesManager.syncUpdatesDir(List.of(args[1]));
    }
}
