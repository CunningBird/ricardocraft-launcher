package ru.ricardocraft.backend.base.request.update;

import ru.ricardocraft.backend.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.events.request.UpdateRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;

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
