package ru.ricardocraft.bff.base.request.update;

import ru.ricardocraft.bff.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.bff.base.request.Request;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;

public final class ProfilesRequest extends Request<ProfilesRequestEvent> implements WebSocketRequest {

    @Override
    public String getType() {
        return "profiles";
    }
}
