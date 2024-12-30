package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.service.profiles.ClientProfile;

import java.util.List;
import java.util.UUID;


public class ProfilesResponse extends AbstractResponse {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("2f26fbdf-598a-46dd-92fc-1699c0e173b1");
    public List<ClientProfile> profiles;

    public ProfilesResponse(List<ClientProfile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public String getType() {
        return "profiles";
    }
}
