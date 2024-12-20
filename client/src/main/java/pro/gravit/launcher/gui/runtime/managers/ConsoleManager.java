package pro.gravit.launcher.gui.runtime.managers;

import pro.gravit.launcher.gui.base.Launcher;
import pro.gravit.launcher.gui.client.events.ClientUnlockConsoleEvent;
import pro.gravit.launcher.gui.runtime.LauncherEngine;
import pro.gravit.launcher.gui.runtime.console.GetConnectUUIDCommand;
import pro.gravit.launcher.gui.runtime.console.UnlockCommand;
import pro.gravit.launcher.gui.runtime.console.test.PrintHardwareInfoCommand;
import pro.gravit.launcher.gui.utils.command.CommandHandler;
import pro.gravit.launcher.gui.utils.command.JLineCommandHandler;
import pro.gravit.launcher.gui.utils.command.StdCommandHandler;
import pro.gravit.launcher.gui.utils.command.basic.ClearCommand;
import pro.gravit.launcher.gui.utils.command.basic.DebugCommand;
import pro.gravit.launcher.gui.utils.command.basic.GCCommand;
import pro.gravit.launcher.gui.utils.command.basic.HelpCommand;
import pro.gravit.launcher.gui.utils.helper.CommonHelper;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

import java.io.IOException;

public class ConsoleManager {
    public static CommandHandler handler;
    public static Thread thread;
    public static boolean isConsoleUnlock = false;

    public static void initConsole() throws IOException {
        CommandHandler localCommandHandler;
        try {
            Class.forName("org.jline.terminal.Terminal");

            // JLine2 available
            localCommandHandler = new JLineCommandHandler();
            LogHelper.info("JLine2 terminal enabled");
        } catch (ClassNotFoundException ignored) {
            localCommandHandler = new StdCommandHandler(true);
            LogHelper.warning("JLine2 isn't in classpath, using std");
        }
        handler = localCommandHandler;
        registerCommands();
        thread = CommonHelper.newThread("Launcher Console", true, handler);
        thread.start();
    }

    public static void registerCommands() {
        handler.registerCommand("help", new HelpCommand(handler));
        handler.registerCommand("gc", new GCCommand());
        handler.registerCommand("clear", new ClearCommand(handler));
        handler.registerCommand("unlock", new UnlockCommand());
        handler.registerCommand("printhardware", new PrintHardwareInfoCommand());
        handler.registerCommand("getconnectuuid", new GetConnectUUIDCommand());
    }

    public static boolean checkUnlockKey(String key) {
        return key.equals(Launcher.getConfig().unlockSecret);
    }

    public static boolean unlock() {
        if (isConsoleUnlock) return true;
        ClientUnlockConsoleEvent event = new ClientUnlockConsoleEvent(handler);
        LauncherEngine.modulesManager.invokeEvent(event);
        if (event.isCancel()) return false;
        handler.registerCommand("debug", new DebugCommand());
        handler.unregisterCommand("unlock");
        isConsoleUnlock = true;
        return true;
    }
}
