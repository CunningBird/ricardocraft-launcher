package ru.ricardocraft.backend.command.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;
import ru.ricardocraft.backend.manangers.mirror.InstallClient;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.MirrorWorkspaceProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InstallClientCommand extends Command {

    private final transient LaunchServerProperties config;
    private final transient InstallClient installClient;

    @Autowired
    public InstallClientCommand(LaunchServerProperties config, InstallClient installClient) {
        super();
        this.config = config;
        this.installClient = installClient;
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
        Version version = parseClientVersion(args[1]);
        VersionType versionType = VersionType.valueOf(args[2]);
        List<String> mods = new ArrayList<>();
        MirrorWorkspaceProperties mirrorWorkspace = config.getMirror().getWorkspace();
        if (mirrorWorkspace != null) {
            switch (versionType) {
                case VANILLA -> {
                }
                case FABRIC -> mods.addAll(config.getMirror().getWorkspace().getFabricMods());
                case FORGE -> mods.addAll(config.getMirror().getWorkspace().getForgeMods());
                case QUILT -> mods.addAll(config.getMirror().getWorkspace().getQuiltMods());
            }
        }
        if (args.length > 3) {
            mods = Arrays.stream(args[3].split(",")).toList();
        }
        installClient.run(name, version, mods, versionType);
    }

    protected Version parseClientVersion(String arg) throws CommandException {
        if (arg.isEmpty()) throw new CommandException("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
