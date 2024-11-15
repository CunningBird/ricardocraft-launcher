package ru.ricardocraft.backend.components;

public abstract class Component {
    protected transient String componentName;

    public final void setComponentName(String s) {
        this.componentName = s;
    }
}
