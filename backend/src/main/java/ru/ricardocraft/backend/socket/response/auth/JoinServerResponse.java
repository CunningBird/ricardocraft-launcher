package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.UUID;

public class JoinServerResponse extends SimpleResponse {
    public String serverID;
    public String accessToken;
    public String username;
    public UUID uuid;

    @Override
    public String getType() {
        return "joinServer";
    }
}
