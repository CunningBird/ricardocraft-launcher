package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.base.modules.events.InitPhase;
import pro.gravit.launcher.gui.client.ClientParams;

public class ClientProcessInitPhase extends InitPhase {
    public final ClientParams params;

    public ClientProcessInitPhase(ClientParams params) {
        this.params = params;
    }
}
