package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetAvailabilityAuthResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "getAvailabilityAuth";
    }
}
