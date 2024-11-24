package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.events.InitPhase;
import ru.ricardocraft.client.client.ClientParams;

public class ClientProcessInitPhase extends InitPhase {
    public final ClientParams params;

    public ClientProcessInitPhase(ClientParams params) {
        this.params = params;
    }
}
