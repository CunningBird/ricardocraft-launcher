package ru.ricardocraft.backend.command.updates.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.util.UUID;

@Component
public class SaveProfilesCommand extends Command {

    private transient final LaunchServerConfig config;
    private transient final NettyServerSocketHandler nettyServerSocketHandler;

    @Autowired
    public SaveProfilesCommand(LaunchServerConfig config, NettyServerSocketHandler nettyServerSocketHandler) {
        super();
        this.config = config;
        this.nettyServerSocketHandler = nettyServerSocketHandler;
    }

    @Override
    public String getArgsDescription() {
        return "[profile names...]";
    }

    @Override
    public String getUsageDescription() {
        return "load and save profile";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        if (args.length > 0) {
            for (String profileName : args) {
                ClientProfile profile;
                try {
                    UUID uuid = UUID.fromString(profileName);
                    profile = config.profileProvider.getProfile(uuid);
                } catch (IllegalArgumentException ex) {
                    profile = config.profileProvider.getProfile(profileName);
                }
                config.profileProvider.addProfile(profile);
            }
            config.profileProvider.syncProfilesDir(config, nettyServerSocketHandler);
        }
    }

}
