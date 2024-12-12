package ru.ricardocraft.client.scenes.settings.components;

import javafx.util.StringConverter;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.service.LaunchService;

public class ThemeConverter extends StringConverter<RuntimeSettings.LAUNCHER_THEME> {

    private final LaunchService launchService;

    public ThemeConverter(LaunchService launchService) {
        this.launchService = launchService;
    }

    @Override
    public String toString(RuntimeSettings.LAUNCHER_THEME object) {
        if (object == null) return "Unknown";
        return launchService.getTranslation(String.format("runtime.themes.%s", object.displayName), object.displayName);
    }

    @Override
    public RuntimeSettings.LAUNCHER_THEME fromString(String string) {
        return null;
    }
}