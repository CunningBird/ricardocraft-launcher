package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.profiles.PlayerProfile;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

import java.util.UUID;


public class ProfileByUUIDRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("b9014cf3-4b95-4d38-8c5f-867f190a18a0");
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;

    public ProfileByUUIDRequestEvent(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public ProfileByUUIDRequestEvent() {
    }

    @Override
    public String getType() {
        return "profileByUUID";
    }
}
