package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.PlayerProfile;

import java.util.UUID;


public class ProfileByUsernameRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("06204302-ff6b-4779-b97d-541e3bc39aa1");

    public final PlayerProfile playerProfile;
    public String error;

    public ProfileByUsernameRequestEvent(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @Override
    public String getType() {
        return "profileByUsername";
    }
}
