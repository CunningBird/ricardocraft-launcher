package pro.gravit.launchserver.modules.impl;

import pro.gravit.launchserver.base.modules.LauncherInitContext;
import pro.gravit.launchserver.LaunchServer;

public class LaunchServerInitContext implements LauncherInitContext {
    public final LaunchServer server;

    public LaunchServerInitContext(LaunchServer server) {
        this.server = server;
    }
}
