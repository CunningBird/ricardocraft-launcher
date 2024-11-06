package ru.ricardocraft.bff.base.events.request;

import ru.ricardocraft.bff.base.events.RequestEvent;
import ru.ricardocraft.bff.base.profiles.PlayerProfile;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

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
