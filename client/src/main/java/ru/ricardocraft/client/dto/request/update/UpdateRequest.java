package ru.ricardocraft.client.dto.request.update;

import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.websockets.WebSocketRequest;
import ru.ricardocraft.client.dto.response.UpdateRequestEvent;

public final class UpdateRequest extends Request<UpdateRequestEvent> implements WebSocketRequest {

    // Instance
    public final String dirName;

    public UpdateRequest(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public String getType() {
        return "update";
    }
}
