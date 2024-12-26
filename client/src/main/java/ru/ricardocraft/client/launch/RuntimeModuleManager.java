package ru.ricardocraft.client.launch;

import org.springframework.stereotype.Component;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.modules.LauncherModule;
import ru.ricardocraft.client.modules.impl.SimpleModuleManager;
import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.client.RuntimeLauncherCoreModule;
import ru.ricardocraft.client.config.LauncherConfig;

import java.util.Collections;
import java.util.List;

@Component
public final class RuntimeModuleManager extends SimpleModuleManager {

    public RuntimeModuleManager() {
        super(null, null, Launcher.getConfig().trustManager);
        LauncherConfig.initModules(this);
        loadModule(new RuntimeLauncherCoreModule());
        initModules(null);

        AuthRequest.registerProviders();
        GetAvailabilityAuthRequest.registerProviders();
        OptionalAction.registerProviders();
        OptionalTrigger.registerProviders();
    }

    @Override
    public LauncherModule loadModule(LauncherModule module) {
        return super.loadModule(module);
    }

    public List<LauncherModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

}
