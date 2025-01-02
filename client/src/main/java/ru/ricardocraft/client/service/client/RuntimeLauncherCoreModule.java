package ru.ricardocraft.client.service.client;

import ru.ricardocraft.client.service.modules.LauncherInitContext;
import ru.ricardocraft.client.service.modules.LauncherModule;
import ru.ricardocraft.client.service.modules.LauncherModuleInfo;
import ru.ricardocraft.client.base.utils.Version;

public class RuntimeLauncherCoreModule extends LauncherModule {
    public RuntimeLauncherCoreModule() {
        super(new LauncherModuleInfo("ClientLauncherCore", Version.getVersion()));
    }

    @Override
    public void init(LauncherInitContext initContext) {

    }
}
