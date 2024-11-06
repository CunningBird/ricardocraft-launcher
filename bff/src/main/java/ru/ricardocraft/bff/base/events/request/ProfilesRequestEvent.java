package ru.ricardocraft.bff.base.events.request;

import ru.ricardocraft.bff.base.events.RequestEvent;
import ru.ricardocraft.bff.base.profiles.ClientProfile;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

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

    public ProfilesRequestEvent() {
    }

    @Override
    public String getType() {
        return "profiles";
    }
}
