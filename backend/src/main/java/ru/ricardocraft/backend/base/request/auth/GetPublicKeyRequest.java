package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.GetPublicKeyRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class GetPublicKeyRequest extends Request<GetPublicKeyRequestEvent> {
    public GetPublicKeyRequest() {
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}