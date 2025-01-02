package ru.ricardocraft.client.service.profiles;

import ru.ricardocraft.client.service.profiles.optional.OptionalFile;
import ru.ricardocraft.client.base.utils.launch.LaunchOptions;

import java.util.*;

public class ClientProfileBuilder {
    private String title;
    private UUID uuid;
    private ClientProfile.Version version;
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
    private List<ClientProfile.ServerProfile> servers;
    private ClientProfile.ClassLoaderConfig classLoaderConfig;
    private List<ClientProfile.CompatibilityFlags> flags;
    private int recommendJavaVersion;
    private int minJavaVersion;
    private int maxJavaVersion;
    private ClientProfile.ProfileDefaultSettings settings;
    private boolean limited;
    private String mainClass;
    private String mainModule;
    private LaunchOptions.ModuleConf moduleConf;

    public ClientProfileBuilder(ClientProfile profile) {
        this.title = profile.getTitle();
        this.uuid = profile.getUUID();
        this.version = profile.getVersion();
        this.info = profile.getInfo();
        this.dir = profile.getDir();
        this.sortIndex = profile.getSortIndex();
        this.assetIndex = profile.getAssetIndex();
        this.assetDir = profile.getAssetDir();
        this.update = new ArrayList<>(profile.getUpdate());
        this.updateExclusions = new ArrayList<>(profile.getUpdateExclusions());
        this.updateVerify = new ArrayList<>(profile.getUpdateVerify());
        this.updateOptional = new HashSet<>(profile.getOptional());
        this.jvmArgs = new ArrayList<>(profile.getJvmArgs());
        this.classPath = new ArrayList<>(profile.getClassPath());
        this.altClassPath = new ArrayList<>(profile.getAlternativeClassPath());
        this.clientArgs = new ArrayList<>(profile.getClientArgs());
        this.compatClasses = new ArrayList<>(profile.getCompatClasses());
        this.loadNatives = new ArrayList<>(profile.getLoadNatives());
        this.properties = new HashMap<>(profile.getProperties());
        this.servers = new ArrayList<>(profile.getServers());
        this.classLoaderConfig = profile.getClassLoaderConfig();
        this.flags = new ArrayList<>(profile.getFlags());
        this.recommendJavaVersion = profile.getRecommendJavaVersion();
        this.minJavaVersion = profile.getMinJavaVersion();
        this.maxJavaVersion = profile.getMaxJavaVersion();
        this.settings = profile.getSettings();
        this.limited = profile.isLimited();
        this.mainClass = profile.getMainClass();
        this.mainModule = profile.getMainModule();
        this.moduleConf = profile.getModuleConf();
    }

    public ClientProfileBuilder setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public ClientProfileBuilder setVersion(ClientProfile.Version version) {
        this.version = version;
        return this;
    }

    public ClientProfileBuilder setInfo(String info) {
        this.info = info;
        return this;
    }

    public ClientProfileBuilder setDir(String dir) {
        this.dir = dir;
        return this;
    }

    public ClientProfileBuilder setUpdate(List<String> update) {
        this.update = update;
        return this;
    }

    public ClientProfileBuilder update(String value) {
        this.update.add(value);
        return this;
    }

    public void setUpdateExclusions(List<String> updateExclusions) {
        this.updateExclusions = updateExclusions;
    }

    public void setUpdateVerify(List<String> updateVerify) {
        this.updateVerify = updateVerify;
    }


    public ClientProfileBuilder setSettings(ClientProfile.ProfileDefaultSettings settings) {
        this.settings = settings;
        return this;
    }

    public ClientProfile createClientProfile() {
        return new ClientProfile(
                title,
                uuid,
                version,
                info,
                dir,
                sortIndex,
                assetIndex,
                assetDir,
                update,
                updateExclusions,
                updateVerify,
                updateOptional,
                jvmArgs,
                classPath,
                altClassPath,
                clientArgs,
                compatClasses,
                loadNatives,
                properties,
                servers,
                classLoaderConfig,
                flags,
                recommendJavaVersion,
                minJavaVersion,
                maxJavaVersion,
                settings,
                limited,
                mainClass,
                mainModule,
                moduleConf
        );
    }
}