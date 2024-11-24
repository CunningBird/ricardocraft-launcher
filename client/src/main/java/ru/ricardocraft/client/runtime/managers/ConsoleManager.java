package ru.ricardocraft.client.runtime.managers;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.client.events.ClientUnlockConsoleEvent;
import ru.ricardocraft.client.runtime.console.GetConnectUUIDCommand;
import ru.ricardocraft.client.runtime.console.UnlockCommand;
import ru.ricardocraft.client.runtime.console.test.PrintHardwareInfoCommand;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.command.JLineCommandHandler;
import ru.ricardocraft.client.utils.command.StdCommandHandler;
import ru.ricardocraft.client.utils.command.basic.ClearCommand;
import ru.ricardocraft.client.utils.command.basic.DebugCommand;
import ru.ricardocraft.client.utils.command.basic.GCCommand;
import ru.ricardocraft.client.utils.command.basic.HelpCommand;
import ru.ricardocraft.client.utils.helper.CommonHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

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
        JavaFXApplication.modulesManager.invokeEvent(event);
        if (event.isCancel()) return false;
        handler.registerCommand("debug", new DebugCommand());
        handler.unregisterCommand("unlock");
        isConsoleUnlock = true;
        return true;
    }
}
