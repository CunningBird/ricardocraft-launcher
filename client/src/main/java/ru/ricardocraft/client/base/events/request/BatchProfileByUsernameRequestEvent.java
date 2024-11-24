package ru.ricardocraft.client.base.events.request;

import ru.ricardocraft.client.base.events.RequestEvent;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

public class BatchProfileByUsernameRequestEvent extends RequestEvent {
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public PlayerProfile[] playerProfiles;

    public BatchProfileByUsernameRequestEvent(PlayerProfile[] profiles) {
        this.playerProfiles = profiles;
    }

    public BatchProfileByUsernameRequestEvent() {
    }

    @Override
    public String getType() {
        return "batchProfileByUsername";
    }
}
