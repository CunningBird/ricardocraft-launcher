package ru.ricardocraft.client.runtime;

import ru.ricardocraft.client.core.LauncherNetworkAPI;
import ru.ricardocraft.client.runtime.client.UserSettings;

import java.util.HashMap;
import java.util.Map;

public class NewLauncherSettings {
    @LauncherNetworkAPI
    public Map<String, UserSettings> userSettings = new HashMap<>();
    @LauncherNetworkAPI
    public String consoleUnlockKey;
}
