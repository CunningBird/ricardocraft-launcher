package ru.ricardocraft.backend.dto.events.request.auth;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.profiles.PlayerProfile;

import java.util.Map;
import java.util.UUID;


public class CheckServerRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID _uuid = UUID.fromString("8801d07c-51ba-4059-b61d-fe1f1510b28a");
    @LauncherNetworkAPI
    public UUID uuid;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;
    @LauncherNetworkAPI
    public String sessionId;
    @LauncherNetworkAPI
    public String hardwareId;
    @LauncherNetworkAPI
    public Map<String, String> sessionProperties;

    public CheckServerRequestEvent() {
    }

    @Override
    public String getType() {
        return "checkServer";
    }
}