package pro.gravit.launcher.gui.base.request.auth;

import pro.gravit.launcher.gui.base.events.request.CurrentUserRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class CurrentUserRequest extends Request<CurrentUserRequestEvent> {
    @Override
    public String getType() {
        return "currentUser";
    }
}
