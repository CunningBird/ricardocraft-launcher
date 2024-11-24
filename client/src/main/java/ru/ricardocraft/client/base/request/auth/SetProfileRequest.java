package ru.ricardocraft.client.base.request.auth;

import ru.ricardocraft.client.base.events.request.SetProfileRequestEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

public class SetProfileRequest extends Request<SetProfileRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String client;

    public SetProfileRequest(ClientProfile profile) {
        this.client = profile.getTitle();
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}
