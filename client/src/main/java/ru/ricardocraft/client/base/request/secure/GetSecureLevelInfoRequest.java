package ru.ricardocraft.client.base.request.secure;

import ru.ricardocraft.client.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.client.base.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
