package ru.ricardocraft.backend.base.request.update;

import ru.ricardocraft.backend.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;

public final class ProfilesRequest extends Request<ProfilesRequestEvent> implements WebSocketRequest {

    @Override
    public String getType() {
        return "profiles";
    }
}
