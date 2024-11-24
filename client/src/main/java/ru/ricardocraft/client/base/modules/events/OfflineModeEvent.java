package ru.ricardocraft.client.base.modules.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.base.request.RequestService;

public class OfflineModeEvent extends LauncherModule.Event {
    public RequestService service;

    public OfflineModeEvent(RequestService service) {
        this.service = service;
    }
}
