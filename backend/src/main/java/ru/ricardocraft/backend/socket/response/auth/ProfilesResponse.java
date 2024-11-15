package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class ProfilesResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "profiles";
    }
}
