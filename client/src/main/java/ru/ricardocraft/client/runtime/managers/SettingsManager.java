package ru.ricardocraft.client.runtime.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.core.managers.GsonManager;
import ru.ricardocraft.client.runtime.NewLauncherSettings;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;

@Component
public class SettingsManager {

    public static NewLauncherSettings settings;

    private final RuntimeSettings runtimeSettings;
    private final GsonManager gsonManager;

    private final Type type;
    private final Path configPath;

    @Autowired
    public SettingsManager(GsonManager gsonManager, GuiModuleConfig guiModuleConfig) throws IOException {
        this.gsonManager = gsonManager;

        this.type = NewLauncherSettings.class;
        this.configPath = DirBridge.dir.resolve("settings.json");

        if (!IOHelper.isFile(configPath)) {
            setConfig(getDefaultConfig());
            saveConfig(configPath);
        }

        try (BufferedReader reader = IOHelper.newReader(configPath)) {
            NewLauncherSettings value = gsonManager.configGson.fromJson(reader, type);
            if (value == null) {
                LogHelper.warning("Config %s is null", configPath);
                setConfig(getDefaultConfig());
                saveConfig(configPath);
            }
            setConfig(value);
        } catch (Exception e) {
            LogHelper.error(e);
            setConfig(getDefaultConfig());
            saveConfig(configPath);
        }

        if (settings.userSettings.get(GsonManager.RUNTIME_NAME) == null) {
            runtimeSettings = RuntimeSettings.getDefault(guiModuleConfig);
            settings.userSettings.put(GsonManager.RUNTIME_NAME, runtimeSettings);
        } else {
            runtimeSettings = (RuntimeSettings) settings.userSettings.get(GsonManager.RUNTIME_NAME);
        }

        runtimeSettings.apply();
        System.setProperty("prism.vsync", String.valueOf(runtimeSettings.globalSettings.prismVSync));
        DirBridge.dirUpdates = runtimeSettings.updatesDir == null ? DirBridge.defaultUpdatesDir : runtimeSettings.updatesDir;
        if (runtimeSettings.locale == null) runtimeSettings.locale = RuntimeSettings.DEFAULT_LOCALE;
    }

    public NewLauncherSettings getConfig() {
        return settings;
    }

    public RuntimeSettings getRuntimeSettings() {
        return runtimeSettings;
    }

    public void setConfig(NewLauncherSettings config) {
        settings = config;
    }

    public NewLauncherSettings getDefaultConfig() {
        NewLauncherSettings newLauncherSettings = new NewLauncherSettings();
        newLauncherSettings.userSettings.put(GsonManager.RUNTIME_NAME, RuntimeSettings.getDefault(new GuiModuleConfig()));
        return newLauncherSettings;
    }

    public void saveConfig() throws IOException {
        saveConfig(configPath);
    }

    void saveConfig(Path configPath) throws IOException {
        try (BufferedWriter writer = IOHelper.newWriter(configPath)) {
            gsonManager.configGson.toJson(getConfig(), type, writer);
        }
    }
}
