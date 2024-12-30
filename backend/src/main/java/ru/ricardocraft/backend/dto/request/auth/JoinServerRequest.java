package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

import java.util.UUID;

public class JoinServerRequest extends AbstractRequest {
    public String serverID;
    public String accessToken;
    public String username;
    public UUID uuid;
}
