package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class CurrentUserResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "currentUser";
    }
}
