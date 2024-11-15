package ru.ricardocraft.backend.socket.response.management;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetConnectUUIDResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getConnectUUID";
    }
}
