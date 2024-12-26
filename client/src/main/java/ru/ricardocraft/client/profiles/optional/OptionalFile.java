package ru.ricardocraft.client.profiles.optional;

import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

import java.util.List;
import java.util.Objects;

public class OptionalFile {
    @LauncherNetworkAPI
    public List<OptionalAction> actions;
    @LauncherNetworkAPI
    public boolean mark;
    @LauncherNetworkAPI
    public boolean visible = true;
    @LauncherNetworkAPI
    public String name;
    @LauncherNetworkAPI
    public String info;
    @LauncherNetworkAPI
    public List<OptionalTrigger> triggersList;
    @LauncherNetworkAPI
    public OptionalDepend[] dependenciesFile;
    @LauncherNetworkAPI
    public OptionalDepend[] conflictFile;
    @LauncherNetworkAPI
    public OptionalDepend[] groupFile;
    @LauncherNetworkAPI
    public transient OptionalFile[] dependencies;
    @LauncherNetworkAPI
    public transient OptionalFile[] conflict;
    @LauncherNetworkAPI
    public transient OptionalFile[] group;
    @LauncherNetworkAPI
    public int subTreeLevel = 1;
    @LauncherNetworkAPI
    public boolean isPreset;
    @LauncherNetworkAPI
    public boolean limited;

    @LauncherNetworkAPI
    public String category;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionalFile that = (OptionalFile) o;
        return Objects.equals(name, that.name);
    }

    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

}
