package ru.ricardocraft.backend.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.command.GenerateCertificateCommand;
import ru.ricardocraft.backend.command.OSSLSignEXECommand;
import ru.ricardocraft.backend.command.basic.*;
import ru.ricardocraft.backend.command.mirror.*;
import ru.ricardocraft.backend.command.remotecontrol.RemoteControlCommand;
import ru.ricardocraft.backend.command.service.*;
import ru.ricardocraft.backend.command.tools.SignDirCommand;
import ru.ricardocraft.backend.command.tools.SignJarCommand;
import ru.ricardocraft.backend.command.unsafe.*;
import ru.ricardocraft.backend.command.updates.DownloadAssetCommand;
import ru.ricardocraft.backend.command.updates.DownloadClientCommand;
import ru.ricardocraft.backend.command.updates.IndexAssetCommand;
import ru.ricardocraft.backend.command.updates.UnindexAssetCommand;
import ru.ricardocraft.backend.command.updates.profile.ProfilesCommand;
import ru.ricardocraft.backend.command.updates.sync.SyncCommand;
import ru.ricardocraft.backend.command.utls.BaseCommandCategory;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.command.utls.JLineCommandHandler;
import ru.ricardocraft.backend.command.utls.StdCommandHandler;

import java.util.List;

@Configuration
public class CommandConfiguration {

    private static final Logger logger = LogManager.getLogger();

    @Bean
    public CommandHandler commandHandler(List<CommandHandler.Category> categories,
                                         GenerateCertificateCommand generateCertificateCommand,
                                         OSSLSignEXECommand osslSignEXECommand,
                                         RemoteControlCommand remoteControlCommand) {
        CommandHandler commandHandler;
        try {
            Class.forName("org.jline.terminal.Terminal");
            // JLine2 available
            commandHandler = new JLineCommandHandler();
            logger.info("JLine2 terminal enabled");
        } catch (Exception ignored) {
            commandHandler = new StdCommandHandler(true);
            logger.warn("JLine2 isn't in classpath, using std");
        }

        categories.forEach(commandHandler::registerCategory);

        commandHandler.registerCommand("clear", new ClearCommand(commandHandler));
        commandHandler.registerCommand("help", new HelpCommand(commandHandler));

        commandHandler.registerCommand("generatecertificate", generateCertificateCommand);
        commandHandler.registerCommand("osslsignexe", osslSignEXECommand);
        commandHandler.registerCommand("remotecontrol", remoteControlCommand);

        return commandHandler;
    }

    @Bean
    public CommandHandler.Category basicCommandCategory(BuildCommand buildCommand,
                                                        DebugCommand debugCommand,
                                                        GCCommand gcCommand,
                                                        StopCommand stopCommand,
                                                        VersionCommand versionCommand) {
        BaseCommandCategory basic = new BaseCommandCategory();
        basic.registerCommand("build", buildCommand);
        basic.registerCommand("debug", debugCommand);
        basic.registerCommand("gc", gcCommand);
        basic.registerCommand("stop", stopCommand);
        basic.registerCommand("version", versionCommand);
        return new CommandHandler.Category(basic, "basic", "Base LaunchServer commands");
    }

    @Bean
    public CommandHandler.Category serviceCommandCategory(ConfigCommand configCommand,
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
        return new CommandHandler.Category(service, "service", "Managing LaunchServer Components");
    }

    @Bean
    public CommandHandler.Category updatesCommandCategory(ProfilesCommand profilesCommand,
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
        return new CommandHandler.Category(updates, "updates", "Update and Sync Management");
    }

    @Bean
    public CommandHandler.Category toolsCommandCategory(SignJarCommand signJarCommand,
                                                        SignDirCommand signDirCommand) {
        BaseCommandCategory tools = new BaseCommandCategory();
        tools.registerCommand("signJar", signJarCommand);
        tools.registerCommand("signDir", signDirCommand);
        return new CommandHandler.Category(tools, "tools", "Other tools");
    }

    @Bean
    public CommandHandler.Category unsafeCommandCategory(LoadJarCommand loadJarCommand,
                                                         RegisterComponentCommand registerComponentCommand,
                                                         SendAuthCommand sendAuthCommand,
                                                         PatcherCommand patcherCommand,
                                                         CipherListCommand cipherListCommand) {
        BaseCommandCategory unsafe = new BaseCommandCategory();
        unsafe.registerCommand("loadJar", loadJarCommand);
        unsafe.registerCommand("registerComponent", registerComponentCommand);
        unsafe.registerCommand("sendAuth", sendAuthCommand);
        unsafe.registerCommand("patcher", patcherCommand);
        unsafe.registerCommand("cipherList", cipherListCommand);
        return new CommandHandler.Category(unsafe, "Unsafe");
    }

    @Bean
    public CommandHandler.Category mirrorCommandCategory(CurseforgeCommand curseforgeCommand,
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
        return new CommandHandler.Category(mirror, "mirror");
    }
}
