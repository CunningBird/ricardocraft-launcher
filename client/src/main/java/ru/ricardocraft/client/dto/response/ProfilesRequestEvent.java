package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

import java.util.List;
import java.util.UUID;


public class ProfilesRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("2f26fbdf-598a-46dd-92fc-1699c0e173b1");
    @LauncherNetworkAPI
    public List<ClientProfile> profiles;

    public ProfilesRequestEvent(List<ClientProfile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public String getType() {
        return "profiles";
    }
}
