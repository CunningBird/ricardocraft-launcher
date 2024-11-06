package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.SetProfileRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;

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
