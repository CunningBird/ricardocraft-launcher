package ru.ricardocraft.backend.dto.profile;

import ru.ricardocraft.backend.dto.SimpleResponse;

import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;

    @Override
    public String getType() {
        return "profileByUUID";
    }
}
