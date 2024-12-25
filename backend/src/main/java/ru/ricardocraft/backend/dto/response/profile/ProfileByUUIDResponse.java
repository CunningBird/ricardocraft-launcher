package ru.ricardocraft.backend.dto.response.profile;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;
}
