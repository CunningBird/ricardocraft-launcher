package ru.ricardocraft.backend.service.profiles.optional.triggers;

import ru.ricardocraft.backend.dto.response.auth.ClientPermissions;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

public interface OptionalTriggerContext {
    ClientProfile getProfile();

    String getUsername();

    default ClientPermissions getPermissions() {
        return ClientPermissions.DEFAULT;
    }

    default PlayerProfile getPlayerProfile() {
        return null;
    }
}
