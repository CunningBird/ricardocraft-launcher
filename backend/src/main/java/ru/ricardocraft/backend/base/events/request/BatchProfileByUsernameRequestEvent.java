package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;

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
