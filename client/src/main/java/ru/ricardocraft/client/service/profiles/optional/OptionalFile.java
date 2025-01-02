package ru.ricardocraft.client.service.profiles.optional;

import ru.ricardocraft.client.service.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.service.profiles.optional.triggers.OptionalTrigger;

import java.util.List;
import java.util.Objects;

public class OptionalFile {

    public List<OptionalAction> actions;
    public boolean mark;
    public boolean visible = true;
    public String name;
    public String info;
    public List<OptionalTrigger> triggersList;
    public OptionalDepend[] dependenciesFile;
    public OptionalDepend[] conflictFile;
    public OptionalDepend[] groupFile;
    public transient OptionalFile[] dependencies;
    public transient OptionalFile[] conflict;
    public transient OptionalFile[] group;
    public int subTreeLevel = 1;
    public boolean isPreset;
    public boolean limited;
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
