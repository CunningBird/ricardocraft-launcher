package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.PlayerProfile;

public class BatchProfileByUsernameRequestEvent extends RequestEvent {

    public String error;
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
