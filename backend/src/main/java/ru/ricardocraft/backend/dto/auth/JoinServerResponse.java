package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

import java.util.UUID;

public class JoinServerResponse extends SimpleResponse {
    public String serverID;
    public String accessToken;
    public String username;
    public UUID uuid;
}
