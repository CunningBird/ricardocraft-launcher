package ru.ricardocraft.backend.base.request.secure;

import ru.ricardocraft.backend.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
