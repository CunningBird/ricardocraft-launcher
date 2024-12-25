package ru.ricardocraft.backend.dto.events.request.management;

import ru.ricardocraft.backend.dto.events.RequestEvent;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

public class GetPublicKeyRequestEvent extends RequestEvent {
    public byte[] rsaPublicKey;
    public byte[] ecdsaPublicKey;

    public GetPublicKeyRequestEvent(RSAPublicKey rsaPublicKey, ECPublicKey ecdsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey.getEncoded();
        this.ecdsaPublicKey = ecdsaPublicKey.getEncoded();
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
