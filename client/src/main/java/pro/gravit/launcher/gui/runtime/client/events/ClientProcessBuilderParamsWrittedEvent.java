package pro.gravit.launcher.gui.runtime.client.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.runtime.client.ClientLauncherProcess;

public class ClientProcessBuilderParamsWrittedEvent extends LauncherModule.Event {
    public final ClientLauncherProcess process;

    public ClientProcessBuilderParamsWrittedEvent(ClientLauncherProcess process) {
        this.process = process;
    }
}
