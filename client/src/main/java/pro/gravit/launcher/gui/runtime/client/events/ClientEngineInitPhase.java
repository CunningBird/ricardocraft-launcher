package pro.gravit.launcher.gui.runtime.client.events;

import pro.gravit.launcher.base.modules.events.InitPhase;
import pro.gravit.launcher.gui.runtime.LauncherEngine;

public class ClientEngineInitPhase extends InitPhase {
    public final LauncherEngine engine;

    public ClientEngineInitPhase(LauncherEngine engine) {
        this.engine = engine;
    }
}
