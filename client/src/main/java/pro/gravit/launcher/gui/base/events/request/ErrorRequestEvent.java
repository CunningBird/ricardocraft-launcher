package pro.gravit.launcher.gui.base.events.request;

import pro.gravit.launcher.gui.base.events.RequestEvent;
import pro.gravit.launcher.gui.core.LauncherNetworkAPI;

import java.util.UUID;


public class ErrorRequestEvent extends RequestEvent {
    public static UUID uuid = UUID.fromString("0af22bc7-aa01-4881-bdbb-dc62b3cdac96");
    @LauncherNetworkAPI
    public final String error;

    public ErrorRequestEvent(String error) {
        this.error = error;
    }

    @Override
    public String getType() {
        return "error";
    }
}
