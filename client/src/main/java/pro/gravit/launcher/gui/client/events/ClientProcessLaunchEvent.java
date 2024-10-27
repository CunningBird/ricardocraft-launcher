package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.client.ClientParams;

public class ClientProcessLaunchEvent extends LauncherModule.Event {
    public final ClientParams params;

    public ClientProcessLaunchEvent(ClientParams params) {
        this.params = params;
    }
}
