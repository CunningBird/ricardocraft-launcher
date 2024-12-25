package ru.ricardocraft.client.runtime.managers;

import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.optional.OptionalFile;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.base.profiles.optional.triggers.OptionalTriggerContext;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.core.LauncherNetworkAPI;
import ru.ricardocraft.client.core.managers.GsonManager;
import ru.ricardocraft.client.runtime.NewLauncherSettings;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.JavaService;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.JavaHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class SettingsManager {

    public static NewLauncherSettings settings;

    private final JavaService javaService;
    private final RuntimeSettings runtimeSettings;
    private final GsonManager gsonManager;
    private final AuthService authService;

    private final Type type;
    private final Path configPath;

    private List<ClientProfile> profiles;
    private ClientProfile profile;
    private Map<ClientProfile, OptionalView> optionalViewMap;

    @Autowired
    public SettingsManager(JavaService javaService,
                           GsonManager gsonManager,
                           GuiModuleConfig guiModuleConfig,
                           AuthService authService) throws IOException {
        this.javaService = javaService;
        this.gsonManager = gsonManager;
        this.authService = authService;

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

    public OptionalView getOptionalView() {
        return this.optionalViewMap.get(this.profile);
    }

    public OptionalView getOptionalView(ClientProfile profile) {
        return this.optionalViewMap.get(profile);
    }

    public void setOptionalView(ClientProfile profile, OptionalView view) {
        optionalViewMap.put(profile, view);
    }

    public List<ClientProfile> getProfiles() {
        return profiles;
    }

    public ClientProfile getProfile() {
        return profile;
    }

    public void setProfile(ClientProfile profile) {
        this.profile = profile;
    }

    public void setProfilesResult(ProfilesRequestEvent rawProfilesResult) {
        this.profiles = rawProfilesResult.profiles;
        this.profiles.sort(ClientProfile::compareTo);
        if (this.optionalViewMap == null) this.optionalViewMap = new HashMap<>();
        for (ClientProfile profile : profiles) {
            profile.updateOptionalGraph();
            OptionalView oldView = this.optionalViewMap.get(profile);
            OptionalView newView = oldView != null ? new OptionalView(profile, oldView) : new OptionalView(profile);
            this.optionalViewMap.put(profile, newView);
        }
        for (ClientProfile profile : profiles) {
            process(profile, getOptionalView(profile));
        }
    }

    public void loadAll() throws IOException {
        if (profiles == null) return;
        Path optionsFile = DirBridge.dir.resolve("options.json");
        if (!Files.exists(optionsFile)) return;

        Type collectionType = new TypeToken<List<SettingsManager.OptionalListEntry>>() {
        }.getType();

        try (Reader reader = IOHelper.newReader(optionsFile)) {
            List<SettingsManager.OptionalListEntry> list = Launcher.gsonManager.gson.fromJson(reader, collectionType);
            for (SettingsManager.OptionalListEntry entry : list) {
                ClientProfile selectedProfile = null;
                for (ClientProfile clientProfile : profiles) {
                    if (entry.profileUUID != null
                            ? entry.profileUUID.equals(clientProfile.getUUID())
                            : clientProfile.getTitle().equals(entry.name)) selectedProfile = clientProfile;
                }
                if (selectedProfile == null) {
                    LogHelper.warning("Optional: profile %s(%s) not found", entry.name, entry.profileUUID);
                    continue;
                }
                OptionalView view = optionalViewMap.get(selectedProfile);
                for (SettingsManager.OptionalListEntryPair entryPair : entry.enabled) {
                    try {
                        OptionalFile file = selectedProfile.getOptionalFile(entryPair.name);
                        assert file != null;
                        if (file.visible) {
                            if (entryPair.mark)
                                view.enable(file, entryPair.installInfo != null
                                        && entryPair.installInfo.isManual, null);
                            else view.disable(file, null);
                        }
                    } catch (Exception exc) {
                        LogHelper.warning("Optional: in profile %s markOptional mod %s failed",
                                selectedProfile.getTitle(), entryPair.name);
                    }
                }
            }
        }
    }

    public RuntimeSettings.ProfileSettings getProfileSettings(ClientProfile profile) {
        if (profile == null) throw new NullPointerException("ClientProfile not selected");
        UUID uuid = profile.getUUID();
        RuntimeSettings.ProfileSettings settings = runtimeSettings.profileSettings.get(uuid);
        if (settings == null) {
            settings = RuntimeSettings.ProfileSettings.getDefault(javaService, profile);
            runtimeSettings.profileSettings.put(uuid, settings);
        }
        return settings;
    }

    public RuntimeSettings.ProfileSettings getProfileSettings() {
        return getProfileSettings(getProfile());
    }

    public void process(ClientProfile profile, OptionalView view) {
        SettingsManager.TriggerManagerContext context = new SettingsManager.TriggerManagerContext(profile);
        for (OptionalFile optional : view.all) {
            if (optional.limited) {
                if (!authService.checkPermission("launcher.runtime.optionals.%s.%s.show"
                        .formatted(profile.getUUID(), optional.name.toLowerCase(Locale.ROOT)))) {
                    view.disable(optional, null);
                    optional.visible = false;
                } else {
                    optional.visible = true;
                }
            }
            if (optional.triggersList == null) continue;
            boolean isRequired = false;
            int success = 0;
            int fail = 0;
            for (OptionalTrigger trigger : optional.triggersList) {
                if (trigger.required) isRequired = true;
                if (trigger.check(optional, context)) {
                    success++;
                } else {
                    fail++;
                }
            }
            if (isRequired) {
                if (fail == 0) view.enable(optional, true, null);
                else view.disable(optional, null);
            } else {
                if (success > 0) view.enable(optional, false, null);
            }
        }
    }

    public NewLauncherSettings getDefaultConfig() {
        NewLauncherSettings newLauncherSettings = new NewLauncherSettings();
        newLauncherSettings.userSettings.put(GsonManager.RUNTIME_NAME, RuntimeSettings.getDefault(new GuiModuleConfig()));
        return newLauncherSettings;
    }

    void saveConfig(Path configPath) throws IOException {
        try (BufferedWriter writer = IOHelper.newWriter(configPath)) {
            gsonManager.configGson.toJson(getConfig(), type, writer);
        }
    }

    public void saveSettings() throws IOException {
        saveConfig(configPath);
        try {
            if (profiles == null) return;
            Path optionsFile = DirBridge.dir.resolve("options.json");
            List<SettingsManager.OptionalListEntry> list = new ArrayList<>(5);
            for (ClientProfile clientProfile : profiles) {
                SettingsManager.OptionalListEntry entry = new SettingsManager.OptionalListEntry();
                entry.name = clientProfile.getTitle();
                entry.profileUUID = clientProfile.getUUID();
                OptionalView view = optionalViewMap.get(clientProfile);
                view.all.forEach((optionalFile -> {
                    if (optionalFile.visible) {
                        boolean isEnabled = view.enabled.contains(optionalFile);
                        OptionalView.OptionalFileInstallInfo installInfo = view.installInfo.get(optionalFile);
                        entry.enabled.add(new SettingsManager.OptionalListEntryPair(optionalFile, isEnabled, installInfo));
                    }
                }));
                list.add(entry);
            }
            try (Writer writer = IOHelper.newWriter(optionsFile)) {
                Launcher.gsonManager.gson.toJson(list, writer);
            }
        } catch (Throwable ex) {
            LogHelper.error(ex);
        }
    }

    public void exitLauncher(int code) {
        try {
            saveSettings();
        } catch (Throwable ignored) {
        }
        System.exit(code);
    }

    private class TriggerManagerContext implements OptionalTriggerContext {
        private final ClientProfile profile;

        private TriggerManagerContext(ClientProfile profile) {
            this.profile = profile;
        }

        @Override
        public ClientProfile getProfile() {
            return profile;
        }

        @Override
        public JavaHelper.JavaVersion getJavaVersion() {
            RuntimeSettings.ProfileSettings profileSettings = getProfileSettings(profile);
            for (JavaHelper.JavaVersion version : javaService.javaVersions) {
                if (profileSettings.javaPath != null && profileSettings.javaPath.equals(version.jvmDir.toString())) {
                    return version;
                }
            }
            return JavaHelper.JavaVersion.getCurrentJavaVersion();
        }
    }

    public static class OptionalListEntryPair {
        @LauncherNetworkAPI
        public String name;
        @LauncherNetworkAPI
        public boolean mark;
        @LauncherNetworkAPI
        public OptionalView.OptionalFileInstallInfo installInfo;

        public OptionalListEntryPair(OptionalFile optionalFile, boolean enabled,
                                     OptionalView.OptionalFileInstallInfo installInfo) {
            name = optionalFile.name;
            mark = enabled;
            this.installInfo = installInfo;
        }
    }

    public static class OptionalListEntry {
        @LauncherNetworkAPI
        public List<SettingsManager.OptionalListEntryPair> enabled = new LinkedList<>();
        @LauncherNetworkAPI
        public String name;
        @LauncherNetworkAPI
        public UUID profileUUID;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SettingsManager.OptionalListEntry that = (SettingsManager.OptionalListEntry) o;
            return Objects.equals(profileUUID, that.profileUUID) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, profileUUID);
        }
    }
}
