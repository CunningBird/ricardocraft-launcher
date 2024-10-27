package pro.gravit.launcher.gui.runtime.client.events;

import pro.gravit.launcher.base.modules.LauncherModule;
import pro.gravit.launcher.gui.runtime.client.ClientLauncherProcess;

public class ClientProcessBuilderPreLaunchEvent extends LauncherModule.Event {
    public final ClientLauncherProcess processBuilder;

    public ClientProcessBuilderPreLaunchEvent(ClientLauncherProcess processBuilder) {
        this.processBuilder = processBuilder;
    }
}
