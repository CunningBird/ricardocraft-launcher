package ru.ricardocraft.client.runtime.client.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.runtime.client.ClientLauncherProcess;

public class ClientProcessBuilderLaunchedEvent extends LauncherModule.Event {
    public final ClientLauncherProcess processBuilder;

    public ClientProcessBuilderLaunchedEvent(ClientLauncherProcess processBuilder) {
        this.processBuilder = processBuilder;
    }
}
