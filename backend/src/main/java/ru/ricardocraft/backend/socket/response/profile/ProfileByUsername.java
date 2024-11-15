package ru.ricardocraft.backend.socket.response.profile;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class ProfileByUsername extends SimpleResponse {
    public String username;
    public String client;

    @Override
    public String getType() {
        return "profileByUsername";
    }
}
