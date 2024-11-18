package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

import java.util.UUID;

public class AdditionalDataResponse extends SimpleResponse {
    public String username;
    public UUID uuid;

    @Override
    public String getType() {
        return "additionalData";
    }
}
