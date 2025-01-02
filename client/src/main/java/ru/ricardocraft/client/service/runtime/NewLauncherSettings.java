package ru.ricardocraft.client.service.runtime;

import ru.ricardocraft.client.service.runtime.client.UserSettings;

import java.util.HashMap;
import java.util.Map;

public class NewLauncherSettings {
    public Map<String, UserSettings> userSettings = new HashMap<>();
    public String consoleUnlockKey;
}
