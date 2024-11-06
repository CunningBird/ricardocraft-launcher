package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.base.events.request.SetProfileRequestEvent;
import ru.ricardocraft.bff.base.profiles.ClientProfile;
import ru.ricardocraft.bff.base.request.Request;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

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
