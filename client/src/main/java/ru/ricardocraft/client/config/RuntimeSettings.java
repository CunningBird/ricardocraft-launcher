package ru.ricardocraft.client.config;

import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.response.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.base.helper.JavaHelper;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.service.runtime.client.DirBridge;
import ru.ricardocraft.client.service.runtime.client.UserSettings;
import ru.ricardocraft.client.service.JavaService;
import ru.ricardocraft.client.base.utils.SystemTheme;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RuntimeSettings extends UserSettings {
    public static final LAUNCHER_LOCALE DEFAULT_LOCALE = LAUNCHER_LOCALE.RUSSIAN;
    public transient Path updatesDir;
    public String login;
    public AuthRequest.AuthPasswordInterface password;
    public boolean autoAuth;
    public GetAvailabilityAuthRequestEvent.AuthAvailability lastAuth;
    public String updatesDirPath;
    public UUID lastProfile;
    public volatile LAUNCHER_LOCALE locale;
    public String oauthAccessToken;
    public String oauthRefreshToken;
    public long oauthExpire;
    public volatile LAUNCHER_THEME theme = LAUNCHER_THEME.COMMON;
    public Map<UUID, ProfileSettings> profileSettings = new HashMap<>();
    public List<ClientProfile> profiles;
    public GlobalSettings globalSettings = new GlobalSettings();

    public static RuntimeSettings getDefault(GuiModuleConfig config) {
        RuntimeSettings runtimeSettings = new RuntimeSettings();
        runtimeSettings.autoAuth = false;
        runtimeSettings.updatesDir = DirBridge.defaultUpdatesDir;
        runtimeSettings.locale = config.locale == null
                ? LAUNCHER_LOCALE.RUSSIAN
                : LAUNCHER_LOCALE.valueOf(config.locale);
        try {
            runtimeSettings.theme = SystemTheme.getSystemTheme();
        } catch (Throwable e) {
            runtimeSettings.theme = LAUNCHER_THEME.COMMON;
        }
        return runtimeSettings;
    }

    public void apply() {
        if (updatesDirPath != null) updatesDir = Paths.get(updatesDirPath);
    }

    public enum LAUNCHER_LOCALE {
        RUSSIAN("ru", "Русский"),
        BELARUSIAN("be", "Беларуская"),
        UKRAINIAN("uk", "Українська"),
        POLISH("pl", "Polska"),
        ENGLISH("en", "English");
        public final String name;
        public final String displayName;

        LAUNCHER_LOCALE(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }
    }

    public enum LAUNCHER_THEME {
        COMMON(null, "default"),
        DARK("dark", "dark");
        public final String name;
        public final String displayName;

        LAUNCHER_THEME(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
        }
    }

    public static class ProfileSettings {
        public int ram;
        public boolean debug;
        public boolean fullScreen;
        public boolean autoEnter;
        public String javaPath;
        public boolean waylandSupport;
        public boolean debugSkipUpdate;
        public boolean debugSkipFileMonitor;

        public static ProfileSettings getDefault(JavaService javaService, ClientProfile profile) {
            ProfileSettings settings = new ProfileSettings();
            ClientProfile.ProfileDefaultSettings defaultSettings = profile.getSettings();
            settings.ram = defaultSettings.ram;
            settings.autoEnter = defaultSettings.autoEnter;
            settings.fullScreen = defaultSettings.fullScreen;
            JavaHelper.JavaVersion version = javaService.getRecommendJavaVersion(profile);
            if (version != null) {
                settings.javaPath = version.jvmDir.toString();
            }
            settings.debugSkipUpdate = false;
            settings.debugSkipFileMonitor = false;
            return settings;
        }

        public ProfileSettings() {

        }
    }

    public static class ProfileSettingsView {
        private transient final ProfileSettings settings;
        public int ram;
        public boolean debug;
        public boolean fullScreen;
        public boolean autoEnter;
        public String javaPath;
        public boolean waylandSupport;
        public boolean debugSkipUpdate;
        public boolean debugSkipFileMonitor;

        public ProfileSettingsView(ProfileSettings settings) {
            ram = settings.ram;
            debug = settings.debug;
            fullScreen = settings.fullScreen;
            autoEnter = settings.autoEnter;
            javaPath = settings.javaPath;
            waylandSupport = settings.waylandSupport;
            debugSkipUpdate = settings.debugSkipUpdate;
            debugSkipFileMonitor = settings.debugSkipFileMonitor;
            this.settings = settings;
        }

        public void apply() {
            settings.ram = ram;
            settings.debug = debug;
            settings.autoEnter = autoEnter;
            settings.fullScreen = fullScreen;
            settings.javaPath = javaPath;
            settings.waylandSupport = waylandSupport;
            settings.debugSkipUpdate = debugSkipUpdate;
            settings.debugSkipFileMonitor = debugSkipFileMonitor;
        }
    }

    public static class GlobalSettings {
        public boolean prismVSync = true;
        public boolean debugAllClients = false;
    }
}
