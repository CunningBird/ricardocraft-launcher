package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class CurrentUserResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "currentUser";
    }
}
