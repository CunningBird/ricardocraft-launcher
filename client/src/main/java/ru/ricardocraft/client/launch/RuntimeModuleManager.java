package ru.ricardocraft.client.launch;

import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.base.modules.impl.SimpleModuleManager;

import java.util.Collections;
import java.util.List;

public final class RuntimeModuleManager extends SimpleModuleManager {
    public RuntimeModuleManager() {
        super(null, null, Launcher.getConfig().trustManager);
    }

    @Override
    public LauncherModule loadModule(LauncherModule module) {
        return super.loadModule(module);
    }

    public List<LauncherModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    @Override
    protected ModulesClassLoader createClassLoader() {
        return null;
    }

    public void callWrapper(ClientLauncherWrapperContext context) {
        for (LauncherModule module : modules) {
            if (module instanceof ClientWrapperModule) {
                ((ClientWrapperModule) module).wrapperPhase(context);
            }
        }
    }
}
