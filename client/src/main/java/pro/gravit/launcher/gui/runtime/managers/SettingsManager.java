package pro.gravit.launcher.gui.runtime.managers;

import pro.gravit.launcher.gui.base.config.JsonConfigurable;
import pro.gravit.launcher.gui.runtime.NewLauncherSettings;
import pro.gravit.launcher.gui.runtime.client.DirBridge;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

public class SettingsManager extends JsonConfigurable<NewLauncherSettings> {
    public static NewLauncherSettings settings;


    public SettingsManager() {
        super(NewLauncherSettings.class, DirBridge.dir.resolve("settings.json"));
    }

    @Override
    public NewLauncherSettings getConfig() {
        return settings;
    }

    @Override
    public void setConfig(NewLauncherSettings config) {
        settings = config;
        if (settings.consoleUnlockKey != null && !ConsoleManager.isConsoleUnlock) {
            if (ConsoleManager.checkUnlockKey(settings.consoleUnlockKey)) {
                ConsoleManager.unlock();
                LogHelper.info("Console auto unlocked");
            }
        }
    }

    @Override
    public NewLauncherSettings getDefaultConfig() {
        return new NewLauncherSettings();
    }
}
