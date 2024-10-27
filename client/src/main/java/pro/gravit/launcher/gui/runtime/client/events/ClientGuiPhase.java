package pro.gravit.launcher.gui.runtime.client.events;

import pro.gravit.launcher.base.modules.LauncherModule;
import pro.gravit.launcher.gui.runtime.gui.RuntimeProvider;

public class ClientGuiPhase extends LauncherModule.Event {
    public final RuntimeProvider runtimeProvider;

    public ClientGuiPhase(RuntimeProvider runtimeProvider) {
        this.runtimeProvider = runtimeProvider;
    }
}
