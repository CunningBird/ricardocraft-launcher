package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

import java.util.UUID;

public class AdditionalDataResponse extends SimpleResponse {
    public String username;
    public UUID uuid;
}
