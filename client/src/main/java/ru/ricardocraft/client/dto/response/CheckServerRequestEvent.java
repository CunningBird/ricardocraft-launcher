package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.PlayerProfile;

import java.util.Map;
import java.util.UUID;


public class CheckServerRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID _uuid = UUID.fromString("8801d07c-51ba-4059-b61d-fe1f1510b28a");

    public UUID uuid;
    public PlayerProfile playerProfile;
    public String sessionId;
    public String hardwareId;
    public Map<String, String> sessionProperties;

    public CheckServerRequestEvent(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public CheckServerRequestEvent() {
    }

    @Override
    public String getType() {
        return "checkServer";
    }
}
