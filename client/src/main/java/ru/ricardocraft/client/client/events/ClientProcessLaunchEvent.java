package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.client.ClientParams;

public class ClientProcessLaunchEvent extends LauncherModule.Event {
    public final ClientParams params;

    public ClientProcessLaunchEvent(ClientParams params) {
        this.params = params;
    }
}
