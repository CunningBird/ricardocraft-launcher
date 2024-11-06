package ru.ricardocraft.backend.base.profiles.optional.triggers;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.helper.JavaHelper;

public interface OptionalTriggerContext {
    ClientProfile getProfile();

    String getUsername();

    JavaHelper.JavaVersion getJavaVersion();

    default ClientPermissions getPermissions() {
        return ClientPermissions.DEFAULT;
    }

    default PlayerProfile getPlayerProfile() {
        return null;
    }
}
