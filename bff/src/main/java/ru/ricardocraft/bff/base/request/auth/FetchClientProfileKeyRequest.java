package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.base.events.request.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class FetchClientProfileKeyRequest extends Request<FetchClientProfileKeyRequestEvent> {
    public FetchClientProfileKeyRequest() {
    }

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}
