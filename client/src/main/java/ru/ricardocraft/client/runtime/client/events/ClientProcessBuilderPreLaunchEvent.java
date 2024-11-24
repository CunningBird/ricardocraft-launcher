package ru.ricardocraft.client.runtime.client.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.runtime.client.ClientLauncherProcess;

public class ClientProcessBuilderPreLaunchEvent extends LauncherModule.Event {
    public final ClientLauncherProcess processBuilder;

    public ClientProcessBuilderPreLaunchEvent(ClientLauncherProcess processBuilder) {
        this.processBuilder = processBuilder;
    }
}
