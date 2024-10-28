package pro.gravit.launchserver.modules.impl;

import pro.gravit.launchserver.base.modules.LauncherInitContext;
import pro.gravit.launchserver.base.modules.LauncherModule;
import pro.gravit.launchserver.base.modules.LauncherModuleInfo;
import pro.gravit.launchserver.base.modules.events.InitPhase;
import pro.gravit.launchserver.utils.Version;

public class LaunchServerCoreModule extends LauncherModule {
    public LaunchServerCoreModule() {
        super(new LauncherModuleInfo("LaunchServerCore", Version.getVersion()));
    }

    @Override
    public void init(LauncherInitContext initContext) {
        registerEvent(this::testEvent, InitPhase.class);
    }

    public void testEvent(InitPhase event) {
        //LogHelper.debug("[LaunchServerCore] Event LaunchServerInitPhase passed");
    }

    @Override
    public <T extends Event> boolean registerEvent(EventHandler<T> handle, Class<T> tClass) {
        return super.registerEvent(handle, tClass);
    }
}
