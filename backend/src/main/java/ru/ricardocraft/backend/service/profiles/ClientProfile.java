package ru.ricardocraft.backend.service.profiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.VerifyHelper;
import ru.ricardocraft.backend.dto.updates.*;
import ru.ricardocraft.backend.service.profiles.optional.OptionalDepend;
import ru.ricardocraft.backend.service.profiles.optional.OptionalFile;
import ru.ricardocraft.backend.service.profiles.optional.triggers.OSTrigger;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ClientProfile implements Comparable<ClientProfile> {

    private String title;
    private UUID uuid;
    private Version version;
    private String info;
    private String dir;
    private int sortIndex;
    private String assetIndex;
    private String assetDir;
    private List<String> update;
    private List<String> updateExclusions;
    private List<String> updateVerify;
    private Set<OptionalFile> updateOptional;
    private List<String> jvmArgs;
    private List<String> classPath;
    private List<String> altClassPath;
    private List<String> clientArgs;
    private List<String> compatClasses;
    private List<String> loadNatives;
    private Map<String, String> properties;
    private List<ServerProfile> servers;
    private ClassLoaderConfig classLoaderConfig;
    private List<CompatibilityFlags> flags;
    private int recommendJavaVersion;
    private int minJavaVersion;
    private int maxJavaVersion;
    private ProfileDefaultSettings settings;
    private boolean limited;
    private String mainClass;
    private String mainModule;
    private LaunchOptions.ModuleConf moduleConf;

    public ServerProfile getDefaultServerProfile() {
        for (ServerProfile profile : servers) {
            if (profile.isDefault) return profile;
        }
        return null;
    }

    @Override
    public int compareTo(ClientProfile o) {
        return Integer.compare(getSortIndex(), o.getSortIndex());
    }

    public List<String> getClassPath() {
        return Collections.unmodifiableList(classPath);
    }

    public List<String> getAlternativeClassPath() {
        return Collections.unmodifiableList(altClassPath);
    }

    public List<String> getClientArgs() {
        return Collections.unmodifiableList(clientArgs);
    }

    public List<String> getUpdateExclusions() {
        return Collections.unmodifiableList(updateExclusions);
    }

    public List<String> getUpdate() {
        return Collections.unmodifiableList(update);
    }

    public List<String> getUpdateVerify() {
        return Collections.unmodifiableList(updateVerify);
    }

    public List<String> getJvmArgs() {
        return Collections.unmodifiableList(jvmArgs);
    }

    public Set<OptionalFile> getOptional() {
        return updateOptional;
    }

    @Deprecated
    public boolean isUpdateFastCheck() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", title, uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void verify() {
        // Version
        getVersion();
        IOHelper.verifyFileName(getAssetIndex());

        // Client
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Profile title can't be empty");
        VerifyHelper.verify(getInfo(), VerifyHelper.NOT_EMPTY, "Profile info can't be empty");

        // Client launcher
        VerifyHelper.verify(getTitle(), VerifyHelper.NOT_EMPTY, "Main class can't be empty");
        if (getUUID() == null) {
            throw new IllegalArgumentException("Profile UUID can't be null");
        }
        for (String s : update) {
            if (s == null) throw new IllegalArgumentException("Found null entry in update");
        }
        for (String s : updateVerify) {
            if (s == null) throw new IllegalArgumentException("Found null entry in updateVerify");
        }
        for (String s : updateExclusions) {
            if (s == null) throw new IllegalArgumentException("Found null entry in updateExclusions");
        }

        for (String s : classPath) {
            if (s == null) throw new IllegalArgumentException("Found null entry in classPath");
        }
        for (String s : jvmArgs) {
            if (s == null) throw new IllegalArgumentException("Found null entry in jvmArgs");
        }
        for (String s : clientArgs) {
            if (s == null) throw new IllegalArgumentException("Found null entry in clientArgs");
        }
        for (String s : compatClasses) {
            if (s == null) throw new IllegalArgumentException("Found null entry in compatClasses");
        }
        for (OptionalFile f : updateOptional) {
            if (f == null) throw new IllegalArgumentException("Found null entry in updateOptional");
            if (f.name == null) throw new IllegalArgumentException("Optional: name must not be null");
            if (f.conflictFile != null) for (OptionalDepend s : f.conflictFile) {
                if (s == null)
                    throw new IllegalArgumentException(String.format("Found null entry in updateOptional.%s.conflictFile", f.name));
            }
            if (f.dependenciesFile != null) for (OptionalDepend s : f.dependenciesFile) {
                if (s == null)
                    throw new IllegalArgumentException(String.format("Found null entry in updateOptional.%s.dependenciesFile", f.name));
            }
            if(f.groupFile != null)
                for (OptionalDepend s : f.groupFile) {
                    if (s == null)
                        throw new IllegalArgumentException(String.format("Found null entry in updateOptional.%s.groupFile", f.name));
            }
            if (f.triggersList != null) {
                for (OSTrigger trigger : f.triggersList) {
                    if (trigger == null)
                        throw new IllegalArgumentException(String.format("Found null entry in updateOptional.%s.triggers", f.name));
                }
            }
        }
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public List<String> getCompatClasses() {
        return Collections.unmodifiableList(compatClasses);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientProfile profile = (ClientProfile) o;
        return Objects.equals(uuid, profile.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
