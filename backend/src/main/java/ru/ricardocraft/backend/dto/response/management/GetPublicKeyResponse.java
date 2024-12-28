package ru.ricardocraft.backend.dto.response.management;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;

public class GetPublicKeyResponse extends AbstractResponse {
    public byte[] rsaPublicKey;
    public byte[] ecdsaPublicKey;

    public GetPublicKeyResponse(RSAPublicKey rsaPublicKey, ECPublicKey ecdsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey.getEncoded();
        this.ecdsaPublicKey = ecdsaPublicKey.getEncoded();
    }

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
