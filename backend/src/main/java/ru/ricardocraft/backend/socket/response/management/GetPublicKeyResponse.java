package ru.ricardocraft.backend.socket.response.management;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetPublicKeyResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "getPublicKey";
    }
}
