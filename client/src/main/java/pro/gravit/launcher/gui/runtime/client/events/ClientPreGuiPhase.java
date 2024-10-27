package pro.gravit.launcher.gui.runtime.client.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.runtime.gui.RuntimeProvider;

public class ClientPreGuiPhase extends LauncherModule.Event {
    public RuntimeProvider runtimeProvider;

    public ClientPreGuiPhase(RuntimeProvider runtimeProvider) {
        this.runtimeProvider = runtimeProvider;
    }
}
