package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.CurrentUserRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class CurrentUserRequest extends Request<CurrentUserRequestEvent> {
    @Override
    public String getType() {
        return "currentUser";
    }
}
