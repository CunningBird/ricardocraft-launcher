package ru.ricardocraft.bff.base.request.secure;

import ru.ricardocraft.bff.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
