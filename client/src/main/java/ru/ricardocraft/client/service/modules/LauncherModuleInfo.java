package ru.ricardocraft.client.service.modules;

import ru.ricardocraft.client.base.utils.Version;

public class LauncherModuleInfo {
    public final String name;
    public final Version version;
    public final int priority;
    public final String[] dependencies;
    /**
     * Alternative module names
     */
    public final String[] providers;

    public LauncherModuleInfo(String name, Version version) {
        this.name = name;
        this.version = version;
        this.priority = 0;
        this.dependencies = new String[0];
        providers = new String[0];
    }

    public LauncherModuleInfo(String name) {
        this.name = name;
        this.version = new Version(1, 0, 0);
        this.priority = 0;
        this.dependencies = new String[0];
        providers = new String[0];
    }

}
