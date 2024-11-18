package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class FetchClientProfileKeyResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}
