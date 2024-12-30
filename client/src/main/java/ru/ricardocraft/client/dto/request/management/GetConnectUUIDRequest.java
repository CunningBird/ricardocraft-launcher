package ru.ricardocraft.client.dto.request.management;

import ru.ricardocraft.client.dto.response.GetConnectUUIDRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
