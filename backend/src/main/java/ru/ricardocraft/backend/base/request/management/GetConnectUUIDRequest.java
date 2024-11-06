package ru.ricardocraft.backend.base.request.management;

import ru.ricardocraft.backend.base.events.request.GetConnectUUIDRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
