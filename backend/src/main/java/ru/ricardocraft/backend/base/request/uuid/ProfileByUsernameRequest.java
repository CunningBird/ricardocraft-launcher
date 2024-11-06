package ru.ricardocraft.backend.base.request.uuid;

import ru.ricardocraft.backend.base.events.request.ProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;

public final class ProfileByUsernameRequest extends Request<ProfileByUsernameRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String username;


    public ProfileByUsernameRequest(String username) {
        this.username = username;
    }

    @Override
    public String getType() {
        return "profileByUsername";
    }
}
