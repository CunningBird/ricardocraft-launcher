package ru.ricardocraft.backend.dto.socket.auth;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

import java.util.UUID;

public class AdditionalDataResponse extends SimpleResponse {
    public String username;
    public UUID uuid;
}
