package pro.gravit.launchserver.command.handler;

import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.basic.*;
import pro.gravit.launchserver.command.GenerateCertificateCommand;
import pro.gravit.launchserver.command.hash.*;
import pro.gravit.launchserver.command.mirror.*;
import pro.gravit.launchserver.command.profiles.ProfilesCommand;
import pro.gravit.launchserver.command.remotecontrol.RemoteControlCommand;
import pro.gravit.launchserver.command.service.*;
import pro.gravit.launchserver.command.sync.*;
import pro.gravit.launchserver.command.tools.SignDirCommand;
import pro.gravit.launchserver.command.tools.SignJarCommand;
import pro.gravit.launchserver.command.OSSLSignEXECommand;
import pro.gravit.launchserver.command.unsafe.*;
import pro.gravit.launchserver.command.utls.BaseCommandCategory;
import pro.gravit.launchserver.command.utls.CommandCategory;
import pro.gravit.launchserver.command.basic.ClearCommand;
import pro.gravit.launchserver.command.basic.GCCommand;
import pro.gravit.launchserver.command.basic.HelpCommand;

public abstract class CommandHandler extends pro.gravit.launchserver.command.utls.CommandHandler {
    public static void registerCommands(pro.gravit.launchserver.command.utls.CommandHandler handler, LaunchServer server) {
        BaseCommandCategory basic = new BaseCommandCategory();
        // Register basic commands
        basic.registerCommand("help", new HelpCommand(handler));
        basic.registerCommand("version", new VersionCommand(server));
        basic.registerCommand("build", new BuildCommand(server));
        basic.registerCommand("stop", new StopCommand(server));
        basic.registerCommand("debug", new DebugCommand(server));
        basic.registerCommand("clear", new ClearCommand(handler));
        basic.registerCommand("gc", new GCCommand());
        Category basicCategory = new Category(basic, "basic", "Base LaunchServer commands");
        handler.registerCategory(basicCategory);

        // Register sync commands
        BaseCommandCategory updates = new BaseCommandCategory();
        updates.registerCommand("indexAsset", new IndexAssetCommand(server));
        updates.registerCommand("unindexAsset", new UnindexAssetCommand(server));
        updates.registerCommand("downloadAsset", new DownloadAssetCommand(server));
        updates.registerCommand("downloadClient", new DownloadClientCommand(server));
        updates.registerCommand("sync", new SyncCommand(server));
        updates.registerCommand("profile", new ProfilesCommand(server));
        Category updatesCategory = new Category(updates, "updates", "Update and Sync Management");
        handler.registerCategory(updatesCategory);

        //Register service commands
        BaseCommandCategory service = new BaseCommandCategory();
        service.registerCommand("config", new ConfigCommand(server));
        service.registerCommand("serverStatus", new ServerStatusCommand(server));
        service.registerCommand("notify", new NotifyCommand(server));
        service.registerCommand("component", new ComponentCommand(server));
        service.registerCommand("clients", new ClientsCommand(server));
        service.registerCommand("securitycheck", new SecurityCheckCommand(server));
        service.registerCommand("token", new TokenCommand(server));
        Category serviceCategory = new Category(service, "service", "Managing LaunchServer Components");
        handler.registerCategory(serviceCategory);

        //Register tools commands
        BaseCommandCategory tools = new BaseCommandCategory();
        tools.registerCommand("signJar", new SignJarCommand(server));
        tools.registerCommand("signDir", new SignDirCommand(server));
        Category toolsCategory = new Category(tools, "tools", "Other tools");
        handler.registerCategory(toolsCategory);

        BaseCommandCategory unsafe = new BaseCommandCategory();
        unsafe.registerCommand("loadJar", new LoadJarCommand(server));
        unsafe.registerCommand("registerComponent", new RegisterComponentCommand(server));
        unsafe.registerCommand("sendAuth", new SendAuthCommand(server));
        unsafe.registerCommand("patcher", new PatcherCommand(server));
        unsafe.registerCommand("cipherList", new CipherListCommand(server));
        Category unsafeCategory = new Category(unsafe, "Unsafe");
        handler.registerCategory(unsafeCategory);

        CommandCategory mirror = new BaseCommandCategory();
        mirror.registerCommand("curseforge", new CurseforgeCommand(server));
        mirror.registerCommand("installClient", new InstallClientCommand(server));
        mirror.registerCommand("installMods", new InstallModCommand(server));
        mirror.registerCommand("deduplibraries", new DeDupLibrariesCommand(server));
        mirror.registerCommand("launchInstaller", new LaunchInstallerCommand(server));
        mirror.registerCommand("lwjgldownload", new LwjglDownloadCommand(server));
        mirror.registerCommand("patchauthlib", new PatchAuthlibCommand(server));
        mirror.registerCommand("applyworkspace", new ApplyWorkspaceCommand(server));
        mirror.registerCommand("workspace", new WorkspaceCommand(server));
        Category category = new Category(mirror, "mirror");
        handler.registerCategory(category);

        handler.registerCommand("generatecertificate", new GenerateCertificateCommand(server));
        handler.registerCommand("osslsignexe", new OSSLSignEXECommand(server));
        handler.registerCommand("remotecontrol", new RemoteControlCommand(server));
    }
}
