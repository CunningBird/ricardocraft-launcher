package pro.gravit.launcher.gui.base.modules.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.base.request.RequestService;

public class OfflineModeEvent extends LauncherModule.Event {
    public RequestService service;

    public OfflineModeEvent(RequestService service) {
        this.service = service;
    }
}
