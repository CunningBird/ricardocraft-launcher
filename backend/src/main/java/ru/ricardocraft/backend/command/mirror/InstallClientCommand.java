package ru.ricardocraft.backend.command.mirror;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.command.mirror.installers.FabricInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.QuiltInstallerCommand;
import ru.ricardocraft.backend.command.updates.profile.MakeProfileCommand;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.manangers.mirror.InstallClient;
import ru.ricardocraft.backend.manangers.mirror.MirrorWorkspace;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.manangers.mirror.modapi.ModrinthAPI;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InstallClientCommand extends Command {
    private final transient LaunchServerConfig config;
    private final transient LaunchServerDirectories directories;
    private final transient UpdatesManager updatesManager;
    private final transient MirrorManager mirrorManager;
    private final transient JacksonManager jacksonManager;
    private final transient ProfileProvider profileProvider;
    private final transient ModrinthAPI modrinthAPI;
    private final transient CurseforgeAPI curseforgeApi;

    private final transient FabricInstallerCommand fabricInstallerCommand;
    private final transient QuiltInstallerCommand quiltInstallerCommand;
    private final transient DeDupLibrariesCommand deDupLibrariesCommand;
    private final transient MakeProfileCommand makeProfileCommand;

    @Autowired
    public InstallClientCommand(LaunchServerConfig config,
                                LaunchServerDirectories directories,
                                UpdatesManager updatesManager,
                                MirrorManager mirrorManager,
                                JacksonManager jacksonManager,
                                ModrinthAPI modrinthAPI,
                                CurseforgeAPI curseforgeApi,
                                ProfileProvider profileProvider,
                                FabricInstallerCommand fabricInstallerCommand,
                                QuiltInstallerCommand quiltInstallerCommand,
                                DeDupLibrariesCommand deDupLibrariesCommand,
                                MakeProfileCommand makeProfileCommand) {
        super();
        this.config = config;
        this.directories = directories;
        this.updatesManager = updatesManager;
        this.mirrorManager = mirrorManager;
        this.jacksonManager = jacksonManager;
        this.profileProvider = profileProvider;
        this.modrinthAPI = modrinthAPI;
        this.curseforgeApi = curseforgeApi;

        this.fabricInstallerCommand = fabricInstallerCommand;
        this.quiltInstallerCommand = quiltInstallerCommand;
        this.deDupLibrariesCommand = deDupLibrariesCommand;
        this.makeProfileCommand = makeProfileCommand;
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
        MirrorWorkspace mirrorWorkspace = config.mirrorConfig.workspace;
        if (mirrorWorkspace != null) {
            switch (versionType) {
                case VANILLA -> {
                }
                case FABRIC -> mods.addAll(config.mirrorConfig.workspace.fabricMods());
                case FORGE -> mods.addAll(config.mirrorConfig.workspace.forgeMods());
                case QUILT -> mods.addAll(config.mirrorConfig.workspace.quiltMods());
            }
        }
        if (args.length > 3) {
            mods = Arrays.stream(args[3].split(",")).toList();
        }
        InstallClient run = new InstallClient(config, directories, updatesManager, mirrorManager, jacksonManager,
                profileProvider, modrinthAPI, curseforgeApi, fabricInstallerCommand, quiltInstallerCommand,
                deDupLibrariesCommand, makeProfileCommand, name, version, mods, versionType, mirrorWorkspace);
        run.run();
    }

    protected ClientProfile.Version parseClientVersion(String arg) throws CommandException, JsonProcessingException {
        if(arg.isEmpty()) {
            throw new CommandException("ClientVersion can't be empty");
        }
        return jacksonManager.getMapper().readValue(arg, ClientProfile.Version.class);
    }
}
