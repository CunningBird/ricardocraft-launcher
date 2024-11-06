package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class FetchClientProfileKeyRequest extends Request<FetchClientProfileKeyRequestEvent> {
    public FetchClientProfileKeyRequest() {
    }

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}
