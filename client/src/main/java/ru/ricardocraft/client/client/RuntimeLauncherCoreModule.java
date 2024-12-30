package ru.ricardocraft.client.client;

import ru.ricardocraft.client.modules.LauncherInitContext;
import ru.ricardocraft.client.modules.LauncherModule;
import ru.ricardocraft.client.modules.LauncherModuleInfo;
import ru.ricardocraft.client.utils.Version;

public class RuntimeLauncherCoreModule extends LauncherModule {
    public RuntimeLauncherCoreModule() {
        super(new LauncherModuleInfo("ClientLauncherCore", Version.getVersion()));
    }

    @Override
    public void init(LauncherInitContext initContext) {

    }
}
