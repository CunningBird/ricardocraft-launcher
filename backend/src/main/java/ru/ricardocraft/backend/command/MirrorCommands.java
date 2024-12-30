package ru.ricardocraft.backend.command;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.command.mirror.*;

@ShellComponent
@ShellCommandGroup("mirror")
@RequiredArgsConstructor
public class MirrorCommands {

    private final ApplyWorkspaceService applyWorkspaceService;
    private final CurseForgeGetModService curseForgeGetModService;
    private final CurseForgeGetModFileService curseForgeGetModFileService;
    private final DeDupLibrariesService deDupLibrariesService;
    private final InstallClientService installClientService;
    private final InstallModService installModService;
    private final LaunchInstallerFabricService launchInstallerFabricService;
    private final LaunchInstallerForgeService launchInstallerForgeService;
    private final LaunchInstallerQuiltService launchInstallerQuiltService;
    private final LwjglDownloadService lwjglDownloadService;
    private final PatchAuthlibService patchAuthlibService;
    private final WorkspaceClearClientCacheService workspaceClearClientCacheService;

    @ShellMethod("[path] apply workspace. This action remove your files in workspace!")
    public void applyWorkspace(@ShellOption(defaultValue = ShellOption.NULL) String path) throws Exception {
        applyWorkspaceService.applyWorkspace(path);
    }

    @ShellMethod(value = "[modId] Get mod info by id")
    public void curseForgeGetMod(@ShellOption Long modId) throws Exception {
        curseForgeGetModService.curseForgeGetMod(modId);
    }

    @ShellMethod(value = "[modId] [fileId] Get mod file info by id")
    public void curseForgeGetModFile(@ShellOption Long modId, @ShellOption Long fileId) throws Exception {
        curseForgeGetModFileService.curseForgeGetModFile(modId, fileId);
    }

    @ShellMethod("[clientDir] (ignore lwjgl) remove libraries duplication (excludes lwjgl)")
    public void deDupLibraries(@ShellOption String clientDir, @ShellOption(defaultValue = "true") Boolean isIgnoreLwjgl) throws Exception {
        deDupLibrariesService.deDupLibraries(clientDir, isIgnoreLwjgl);
    }

    @ShellMethod("[name] [version] [versionType] (mods)")
    public void installClient(@ShellOption String name,
                              @ShellOption String clientVersion,
                              @ShellOption String clientVersionType,
                              @ShellOption(defaultValue = ShellOption.NULL) String[] clientMods) throws Exception {
        installClientService.installClient(name, clientVersion, clientVersionType, clientMods);
    }

    @ShellMethod("[dir] [version] [forge/fabric] [mod1,mod2,mod3]")
    public void installMods(@ShellOption String modDir,
                            @ShellOption String modVersion,
                            @ShellOption String loaderName,
                            @ShellOption String[] modsList) throws Exception {
        installModService.installMods(modDir, modVersion, loaderName, modsList);
    }

    @ShellMethod("[minecraft version] [vanilla dir] [fabric installer file] (loader version) install fabric to client")
    public void launchInstallerFabric(@ShellOption String version,
                                      @ShellOption String installerVanillaDir,
                                      @ShellOption String installerFabricInstallerFile,
                                      @ShellOption(defaultValue = ShellOption.NULL) String loaderVersion) throws Exception {
        launchInstallerFabricService.launchInstallerFabric(version, installerVanillaDir, installerFabricInstallerFile, loaderVersion);
    }

    @ShellMethod("[vanilla dir] [forge installer file] install forge to client")
    public void launchInstallerForge(@ShellOption String vanillaDir, @ShellOption String forgeInstallerFile) throws Exception {
        launchInstallerForgeService.launchInstallerForge(vanillaDir, forgeInstallerFile);
    }

    @ShellMethod("[minecraft version] [vanilla dir] [fabric installer file] install quilt to client")
    public void launchInstallerQuilit(@ShellOption String version,
                                      @ShellOption String installerVanillaDir,
                                      @ShellOption String installerFabricFile) throws Exception {
        launchInstallerQuiltService.launchInstallerQuilit(version, installerVanillaDir, installerFabricFile);
    }

    @ShellMethod("[version] [client dir] download lwjgl 3.3.0+")
    public void lwjglDownload(@ShellOption String version, @ShellOption String clientDirectory) throws Exception {
        lwjglDownloadService.lwjglDownload(version, clientDirectory);
    }

    @ShellMethod("[dir] [authlib file] patch client authlib")
    public void patchAuthlib(@ShellOption String patchDirectory, @ShellOption String authlibFile) throws Exception {
        patchAuthlibService.patchAuthlib(patchDirectory, authlibFile);
    }

    @ShellMethod("[vanilla/forge/fabric/neoforge] remove client cache with specific loader and version")
    public void workspaceClearClientCache(@ShellOption String engine) throws Exception {
        workspaceClearClientCacheService.workspaceClearClientCache(engine);
    }
}
