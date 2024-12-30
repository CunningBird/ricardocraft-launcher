package ru.ricardocraft.backend.dto.response.profile;

import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

public class BatchProfileByUsernameResponse extends AbstractResponse {
    public String error;
    public PlayerProfile[] playerProfiles;

    public BatchProfileByUsernameResponse() {
    }

    @Override
    public String getType() {
        return "batchProfileByUsername";
    }
}
