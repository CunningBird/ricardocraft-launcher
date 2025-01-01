package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.profiles.ClientProfile;

import java.util.UUID;


public class SetProfileRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("08c0de9e-4364-4152-9066-8354a3a48541");

    public final ClientProfile newProfile;

    public SetProfileRequestEvent(ClientProfile newProfile) {
        this.newProfile = newProfile;
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}
