package ru.ricardocraft.bff.base.request.update;

import ru.ricardocraft.bff.core.LauncherNetworkAPI;
import ru.ricardocraft.bff.base.events.request.UpdateRequestEvent;
import ru.ricardocraft.bff.base.request.Request;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;

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
