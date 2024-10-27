package pro.gravit.launcher.gui.client;

import pro.gravit.launcher.gui.base.modules.LauncherInitContext;
import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.base.modules.LauncherModuleInfo;
import pro.gravit.launcher.gui.utils.Version;

public class RuntimeLauncherCoreModule extends LauncherModule {
    public RuntimeLauncherCoreModule() {
        super(new LauncherModuleInfo("ClientLauncherCore", Version.getVersion()));
    }

    @Override
    public void init(LauncherInitContext initContext) {

    }
}
