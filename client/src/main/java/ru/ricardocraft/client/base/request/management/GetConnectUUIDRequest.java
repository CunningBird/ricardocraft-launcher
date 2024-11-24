package ru.ricardocraft.client.base.request.management;

import ru.ricardocraft.client.base.events.request.GetConnectUUIDRequestEvent;
import ru.ricardocraft.client.base.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
