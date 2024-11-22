package ru.ricardocraft.backend.dto.socket.profile;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

import java.util.UUID;

public class ProfileByUUIDResponse extends SimpleResponse {
    public UUID uuid;
    public String client;
}
