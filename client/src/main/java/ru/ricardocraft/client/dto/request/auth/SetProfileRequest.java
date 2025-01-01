package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.websockets.WebSocketRequest;
import ru.ricardocraft.client.dto.response.SetProfileRequestEvent;
import ru.ricardocraft.client.profiles.ClientProfile;

public class SetProfileRequest extends Request<SetProfileRequestEvent> implements WebSocketRequest {

    public final String client;

    public SetProfileRequest(ClientProfile profile) {
        this.client = profile.getTitle();
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}
