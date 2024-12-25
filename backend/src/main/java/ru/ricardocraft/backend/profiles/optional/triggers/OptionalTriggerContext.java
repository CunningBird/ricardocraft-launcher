package ru.ricardocraft.backend.profiles.optional.triggers;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.PlayerProfile;

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
