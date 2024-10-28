package pro.gravit.launchserver.base.modules.events;

import pro.gravit.launchserver.base.modules.LauncherModule;
import pro.gravit.launchserver.base.request.RequestService;

public class OfflineModeEvent extends LauncherModule.Event {
    public RequestService service;

    public OfflineModeEvent(RequestService service) {
        this.service = service;
    }
}
