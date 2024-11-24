package ru.ricardocraft.client.base.request.update;

import ru.ricardocraft.client.base.events.request.UpdateRequestEvent;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

public final class UpdateRequest extends Request<UpdateRequestEvent> implements WebSocketRequest {

    // Instance
    @LauncherNetworkAPI
    public final String dirName;

    public UpdateRequest(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public String getType() {
        return "update";
    }
}
