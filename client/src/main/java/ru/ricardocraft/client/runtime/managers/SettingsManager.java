package ru.ricardocraft.client.runtime.managers;

import ru.ricardocraft.client.base.config.JsonConfigurable;
import ru.ricardocraft.client.runtime.NewLauncherSettings;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.utils.helper.LogHelper;

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
