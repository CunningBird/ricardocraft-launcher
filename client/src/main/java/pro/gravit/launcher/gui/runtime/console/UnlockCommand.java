package pro.gravit.launcher.gui.runtime.console;

import pro.gravit.launcher.gui.runtime.managers.ConsoleManager;
import pro.gravit.launcher.gui.runtime.managers.SettingsManager;
import pro.gravit.launcher.gui.utils.command.Command;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

public class UnlockCommand extends Command {
    @Override
    public String getArgsDescription() {
        return "[key]";
    }

    @Override
    public String getUsageDescription() {
        return "Unlock console commands";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        if (ConsoleManager.checkUnlockKey(args[0])) {
            LogHelper.info("Unlock successful");
            if (!ConsoleManager.unlock()) {
                LogHelper.error("Console unlock canceled");
                return;
            }
            LogHelper.info("Write unlock key");
            SettingsManager.settings.consoleUnlockKey = args[0];
        } else {
            LogHelper.error("Unlock key incorrect");
        }
    }
}
