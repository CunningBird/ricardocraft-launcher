package ru.ricardocraft.client.dto.request.secure;

import ru.ricardocraft.client.dto.response.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class GetSecureLevelInfoRequest extends Request<GetSecureLevelInfoRequestEvent> {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }
}
