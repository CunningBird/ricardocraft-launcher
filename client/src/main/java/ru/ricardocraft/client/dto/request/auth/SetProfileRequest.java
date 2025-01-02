package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.client.WebSocketRequest;
import ru.ricardocraft.client.dto.response.SetProfileRequestEvent;
import ru.ricardocraft.client.service.profiles.ClientProfile;

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
