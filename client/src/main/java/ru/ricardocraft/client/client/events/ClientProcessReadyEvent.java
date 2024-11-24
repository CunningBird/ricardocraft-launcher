package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.events.PostInitPhase;
import ru.ricardocraft.client.client.ClientParams;

public class ClientProcessReadyEvent extends PostInitPhase {
    public final ClientParams params;

    public ClientProcessReadyEvent(ClientParams params) {
        this.params = params;
    }
}
