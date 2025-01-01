package ru.ricardocraft.client.runtime;

import ru.ricardocraft.client.runtime.client.UserSettings;

import java.util.HashMap;
import java.util.Map;

public class NewLauncherSettings {
    public Map<String, UserSettings> userSettings = new HashMap<>();
    public String consoleUnlockKey;
}
