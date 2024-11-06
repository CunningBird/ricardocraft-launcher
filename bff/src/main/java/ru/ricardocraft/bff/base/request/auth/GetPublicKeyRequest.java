package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.base.events.request.GetPublicKeyRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class GetPublicKeyRequest extends Request<GetPublicKeyRequestEvent> {
    public GetPublicKeyRequest() {
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
