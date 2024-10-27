package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.gui.base.modules.events.ClosePhase;

public class ClientExitPhase extends ClosePhase {
    public final int code;

    public ClientExitPhase(int code) {
        this.code = code;
    }
}
