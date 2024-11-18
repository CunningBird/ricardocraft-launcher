package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class ProfilesResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "profiles";
    }
}
