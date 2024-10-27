package pro.gravit.launcher.gui.runtime;

import pro.gravit.launcher.core.LauncherNetworkAPI;
import pro.gravit.launcher.gui.runtime.client.UserSettings;

import java.util.HashMap;
import java.util.Map;

public class NewLauncherSettings {
    @LauncherNetworkAPI
    public Map<String, UserSettings> userSettings = new HashMap<>();
    @LauncherNetworkAPI
    public String consoleUnlockKey;
}
