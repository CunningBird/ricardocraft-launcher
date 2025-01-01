package ru.ricardocraft.client.dto.request.update;

import ru.ricardocraft.client.dto.response.ProfilesRequestEvent;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.websockets.WebSocketRequest;

public final class ProfilesRequest extends Request<ProfilesRequestEvent> implements WebSocketRequest {

    @Override
    public String getType() {
        return "profiles";
    }
}
