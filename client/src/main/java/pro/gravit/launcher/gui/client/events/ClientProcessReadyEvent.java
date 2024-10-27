package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.base.modules.events.PostInitPhase;
import pro.gravit.launcher.gui.client.ClientParams;

public class ClientProcessReadyEvent extends PostInitPhase {
    public final ClientParams params;

    public ClientProcessReadyEvent(ClientParams params) {
        this.params = params;
    }
}
