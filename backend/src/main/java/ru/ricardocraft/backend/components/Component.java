package ru.ricardocraft.backend.components;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.utils.ProviderMap;

public abstract class Component {
    public static final ProviderMap<Component> providers = new ProviderMap<>();
    private static boolean registredComp = false;
    protected transient String componentName;

    public static void registerComponents() {
        if (!registredComp) {
            providers.register("authLimiter", AuthLimiterComponent.class);
            providers.register("proguard", ProGuardComponent.class);
            providers.register("whitelist", WhitelistComponent.class);
            registredComp = true;
        }
    }

    public final void setComponentName(String s) {
        this.componentName = s;
    }
}