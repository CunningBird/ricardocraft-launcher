package ru.ricardocraft.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.command.BaseCommandCategory;
import ru.ricardocraft.backend.command.CommandHandler;
import ru.ricardocraft.backend.command.basic.GCCommand;
import ru.ricardocraft.backend.command.basic.StopCommand;
import ru.ricardocraft.backend.command.basic.VersionCommand;
import ru.ricardocraft.backend.command.mirror.*;
import ru.ricardocraft.backend.command.service.*;
import ru.ricardocraft.backend.command.unsafe.CipherListCommand;
import ru.ricardocraft.backend.command.unsafe.LoadJarCommand;
import ru.ricardocraft.backend.command.unsafe.PatcherCommand;
import ru.ricardocraft.backend.command.unsafe.SendAuthCommand;
import ru.ricardocraft.backend.command.updates.*;

@Configuration
public class CommandConfiguration {

    @Bean
    public CommandHandler.Category basicCommandCategory(CommandHandler commandHandler,
                                                        GCCommand gcCommand,
                                                        StopCommand stopCommand,
                                                        VersionCommand versionCommand) {
        BaseCommandCategory basic = new BaseCommandCategory();
        basic.registerCommand("gc", gcCommand);
        basic.registerCommand("stop", stopCommand);
        basic.registerCommand("version", versionCommand);
        CommandHandler.Category category = new CommandHandler.Category(basic, "basic", "Base LaunchServer commands");
        commandHandler.registerCategory(category);
        return category;
    }

    @Bean
    public CommandHandler.Category serviceCommandCategory(CommandHandler commandHandler,
                                                          ConfigCommand configCommand,
                                                          ServerStatusCommand serverStatusCommand,
                                                          NotifyCommand notifyCommand,
                                                          ClientsCommand clientsCommand,
                                                          SecurityCheckCommand securityCheckCommand,
                                                          TokenCommand tokenCommand) {
        BaseCommandCategory service = new BaseCommandCategory();
        service.registerCommand("config", configCommand);
        service.registerCommand("serverStatus", serverStatusCommand);
        service.registerCommand("notify", notifyCommand);
        service.registerCommand("clients", clientsCommand);
        service.registerCommand("securitycheck", securityCheckCommand);
        service.registerCommand("token", tokenCommand);
        CommandHandler.Category category = new CommandHandler.Category(service, "service", "Managing LaunchServer Components");
        commandHandler.registerCategory(category);
        return category;
    }

    @Bean
    public CommandHandler.Category updatesCommandCategory(CommandHandler commandHandler,
                                                          ProfilesCommand profilesCommand,
                                                          SyncCommand syncCommand,
                                                          IndexAssetCommand indexAssetCommand,
                                                          UnindexAssetCommand unindexAssetCommand,
                                                          DownloadAssetCommand downloadAssetCommand,
                                                          DownloadClientCommand downloadClientCommand) {
        BaseCommandCategory updates = new BaseCommandCategory();
        updates.registerCommand("profile", profilesCommand);
        updates.registerCommand("sync", syncCommand);
        updates.registerCommand("indexAsset", indexAssetCommand);
        updates.registerCommand("unindexAsset", unindexAssetCommand);
        updates.registerCommand("downloadAsset", downloadAssetCommand);
        updates.registerCommand("downloadClient", downloadClientCommand);
        CommandHandler.Category category = new CommandHandler.Category(updates, "updates", "Update and Sync Management");
        commandHandler.registerCategory(category);
        return category;
    }

    @Bean
    public CommandHandler.Category unsafeCommandCategory(CommandHandler commandHandler,
                                                         LoadJarCommand loadJarCommand,
                                                         SendAuthCommand sendAuthCommand,
                                                         PatcherCommand patcherCommand,
                                                         CipherListCommand cipherListCommand) {
        BaseCommandCategory unsafe = new BaseCommandCategory();
        unsafe.registerCommand("loadJar", loadJarCommand);
        unsafe.registerCommand("sendAuth", sendAuthCommand);
        unsafe.registerCommand("patcher", patcherCommand);
        unsafe.registerCommand("cipherList", cipherListCommand);
        CommandHandler.Category category = new CommandHandler.Category(unsafe, "Unsafe");
        commandHandler.registerCategory(category);
        return category;
    }

    @Bean
    public CommandHandler.Category mirrorCommandCategory(CommandHandler commandHandler,
                                                         CurseforgeCommand curseforgeCommand,
                                                         InstallClientCommand installClientCommand,
                                                         InstallModCommand installModCommand,
                                                         DeDupLibrariesCommand deDupLibrariesCommand,
                                                         LaunchInstallerCommand launchInstallerCommand,
                                                         LwjglDownloadCommand lwjglDownloadCommand,
                                                         PatchAuthlibCommand patchAuthlibCommand,
                                                         ApplyWorkspaceCommand applyWorkspaceCommand,
                                                         WorkspaceCommand workspaceCommand) {
        BaseCommandCategory mirror = new BaseCommandCategory();
        mirror.registerCommand("curseforge", curseforgeCommand);
        mirror.registerCommand("installClient", installClientCommand);
        mirror.registerCommand("installMods", installModCommand);
        mirror.registerCommand("deduplibraries", deDupLibrariesCommand);
        mirror.registerCommand("launchInstaller", launchInstallerCommand);
        mirror.registerCommand("lwjgldownload", lwjglDownloadCommand);
        mirror.registerCommand("patchauthlib", patchAuthlibCommand);
        mirror.registerCommand("applyworkspace", applyWorkspaceCommand);
        mirror.registerCommand("workspace", workspaceCommand);
        CommandHandler.Category category = new CommandHandler.Category(mirror, "mirror");
        commandHandler.registerCategory(category);
        return category;
    }
}
