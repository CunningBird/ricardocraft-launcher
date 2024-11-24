package ru.ricardocraft.client.config;

import ru.ricardocraft.client.base.modules.JavaRuntimeModule;
import ru.ricardocraft.client.runtime.NewLauncherSettings;
import ru.ricardocraft.client.runtime.managers.SettingsManager;

public class StdSettingsManager extends SettingsManager {

    @Override
    public NewLauncherSettings getDefaultConfig() {
        NewLauncherSettings newLauncherSettings = new NewLauncherSettings();
        newLauncherSettings.userSettings.put(JavaRuntimeModule.RUNTIME_NAME,
                                             RuntimeSettings.getDefault(new GuiModuleConfig()));
        return newLauncherSettings;
    }
}