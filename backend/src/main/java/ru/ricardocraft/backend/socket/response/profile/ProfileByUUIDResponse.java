package ru.ricardocraft.backend.socket.response.profile;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;

    @Override
    public String getType() {
        return "profileByUUID";
    }
}
