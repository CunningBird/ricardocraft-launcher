package ru.ricardocraft.backend.dto.management;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class GetPublicKeyResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
