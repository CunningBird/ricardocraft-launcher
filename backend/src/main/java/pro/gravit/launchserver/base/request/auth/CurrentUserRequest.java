package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.CurrentUserRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class CurrentUserRequest extends Request<CurrentUserRequestEvent> {
    @Override
    public String getType() {
        return "currentUser";
    }
}
