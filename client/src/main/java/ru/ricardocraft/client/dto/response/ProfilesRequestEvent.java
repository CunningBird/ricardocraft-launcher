package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.ClientProfile;

import java.util.List;
import java.util.UUID;


public class ProfilesRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("2f26fbdf-598a-46dd-92fc-1699c0e173b1");

    public List<ClientProfile> profiles;

    public ProfilesRequestEvent(List<ClientProfile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public String getType() {
        return "profiles";
    }
}
