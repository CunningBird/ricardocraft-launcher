package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.UUID;

public class AdditionalDataResponse extends SimpleResponse {
    public String username;
    public UUID uuid;

    @Override
    public String getType() {
        return "additionalData";
    }
}
