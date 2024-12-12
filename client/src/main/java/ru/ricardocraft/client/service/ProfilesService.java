package ru.ricardocraft.client.service;

import com.google.gson.reflect.TypeToken;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.optional.OptionalFile;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.core.LauncherNetworkAPI;
import ru.ricardocraft.client.impl.TriggerManager;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProfilesService {
    private final TriggerManager triggerManager;
    private List<ClientProfile> profiles;
    private ClientProfile profile;
    private Map<ClientProfile, OptionalView> optionalViewMap;

    public ProfilesService(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
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
            triggerManager.process(profile, getOptionalView(profile));
        }
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


    public void saveAll() throws IOException {
        if (profiles == null) return;
        Path optionsFile = DirBridge.dir.resolve("options.json");
        List<OptionalListEntry> list = new ArrayList<>(5);
        for (ClientProfile clientProfile : profiles) {
            OptionalListEntry entry = new OptionalListEntry();
            entry.name = clientProfile.getTitle();
            entry.profileUUID = clientProfile.getUUID();
            OptionalView view = optionalViewMap.get(clientProfile);
            view.all.forEach((optionalFile -> {
                if (optionalFile.visible) {
                    boolean isEnabled = view.enabled.contains(optionalFile);
                    OptionalView.OptionalFileInstallInfo installInfo = view.installInfo.get(optionalFile);
                    entry.enabled.add(new OptionalListEntryPair(optionalFile, isEnabled, installInfo));
                }
            }));
            list.add(entry);
        }
        try (Writer writer = IOHelper.newWriter(optionsFile)) {
            Launcher.gsonManager.gson.toJson(list, writer);
        }
    }

    public void loadAll() throws IOException {
        if (profiles == null) return;
        Path optionsFile = DirBridge.dir.resolve("options.json");
        if (!Files.exists(optionsFile)) return;

        Type collectionType = new TypeToken<List<OptionalListEntry>>() {
        }.getType();

        try (Reader reader = IOHelper.newReader(optionsFile)) {
            List<OptionalListEntry> list = Launcher.gsonManager.gson.fromJson(reader, collectionType);
            for (OptionalListEntry entry : list) {
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
                for (OptionalListEntryPair entryPair : entry.enabled) {
                    try {
                        OptionalFile file = selectedProfile.getOptionalFile(entryPair.name);
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
        public List<OptionalListEntryPair> enabled = new LinkedList<>();
        @LauncherNetworkAPI
        public String name;
        @LauncherNetworkAPI
        public UUID profileUUID;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OptionalListEntry that = (OptionalListEntry) o;
            return Objects.equals(profileUUID, that.profileUUID) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, profileUUID);
        }
    }
}