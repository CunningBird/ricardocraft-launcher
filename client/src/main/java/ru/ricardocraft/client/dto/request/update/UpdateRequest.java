package ru.ricardocraft.client.dto.request.update;

import ru.ricardocraft.client.dto.response.UpdateRequestEvent;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.websockets.WebSocketRequest;
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
