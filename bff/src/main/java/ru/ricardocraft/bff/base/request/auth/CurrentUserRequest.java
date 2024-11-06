package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class CurrentUserRequest extends Request<CurrentUserRequestEvent> {
    @Override
    public String getType() {
        return "currentUser";
    }
}
