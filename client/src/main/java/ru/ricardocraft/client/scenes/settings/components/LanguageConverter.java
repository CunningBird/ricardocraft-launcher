package ru.ricardocraft.client.scenes.settings.components;

import javafx.util.StringConverter;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.service.LaunchService;

public class LanguageConverter extends StringConverter<RuntimeSettings.LAUNCHER_LOCALE> {

    private final LaunchService launchService;

    public LanguageConverter(LaunchService launchService) {
        this.launchService = launchService;
    }

    @Override
    public String toString(RuntimeSettings.LAUNCHER_LOCALE object) {
        if (object == null) return "Unknown";
        return launchService.getTranslation(String.format("runtime.themes.%s", object.displayName), object.displayName);
    }

    @Override
    public RuntimeSettings.LAUNCHER_LOCALE fromString(String string) {
        return null;
    }
}