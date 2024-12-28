package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.profiles.ClientProfile;

import java.util.UUID;


public class SetProfileResponse extends AbstractResponse {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("08c0de9e-4364-4152-9066-8354a3a48541");
    @LauncherNetworkAPI
    public final ClientProfile newProfile;

    public SetProfileResponse(ClientProfile newProfile) {
        this.newProfile = newProfile;
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}
