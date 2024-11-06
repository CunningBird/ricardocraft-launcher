package ru.ricardocraft.bff.base.request.management;

import ru.ricardocraft.bff.base.events.request.GetConnectUUIDRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class GetConnectUUIDRequest extends Request<GetConnectUUIDRequestEvent> {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
