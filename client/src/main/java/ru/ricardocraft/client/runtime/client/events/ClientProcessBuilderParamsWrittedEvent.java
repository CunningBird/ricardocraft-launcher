package ru.ricardocraft.client.runtime.client.events;

import ru.ricardocraft.client.modules.LauncherModule;
import ru.ricardocraft.client.runtime.client.ClientLauncherProcess;

public class ClientProcessBuilderParamsWrittedEvent extends LauncherModule.Event {
    public final ClientLauncherProcess process;

    public ClientProcessBuilderParamsWrittedEvent(ClientLauncherProcess process) {
        this.process = process;
    }
}
