package ru.ricardocraft.bff.command.mirror;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.base.profiles.ClientProfile;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.config.LaunchServerConfig;
import ru.ricardocraft.bff.mirror.InstallClient;
import ru.ricardocraft.bff.mirror.MirrorWorkspace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstallClientCommand extends Command {
    private final LaunchServerConfig.MirrorConfig config;

    public InstallClientCommand(LaunchServer server) {
        super(server);
        this.config = server.config.mirrorConfig;
    }

    @Override
    public String getArgsDescription() {
        return "[name] [version] [versionType] (mods)";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 3);
        String name = args[0];
        ClientProfile.Version version = parseClientVersion(args[1]);
        InstallClient.VersionType versionType = InstallClient.VersionType.valueOf(args[2]);
        List<String> mods = new ArrayList<>();
        MirrorWorkspace mirrorWorkspace = config.workspace;
        if(mirrorWorkspace != null) {
            switch (versionType) {
                case VANILLA -> {
                }
                case FABRIC -> mods.addAll(config.workspace.fabricMods());
                case FORGE -> mods.addAll(config.workspace.forgeMods());
                case QUILT -> mods.addAll(config.workspace.quiltMods());
            }
        }
        if (args.length > 3) {
            mods = Arrays.stream(args[3].split(",")).toList();
        }
        InstallClient run = new InstallClient(server, name, version, mods, versionType, mirrorWorkspace);
        run.run();
    }
}
