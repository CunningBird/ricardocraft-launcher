package ru.ricardocraft.backend.dto.response.profile;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

import java.util.UUID;


public class ProfileByUsernameResponse extends AbstractResponse {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("06204302-ff6b-4779-b97d-541e3bc39aa1");
    @LauncherNetworkAPI
    public final PlayerProfile playerProfile;
    @LauncherNetworkAPI
    public String error;

    public ProfileByUsernameResponse(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @Override
    public String getType() {
        return "profileByUsername";
    }
}
