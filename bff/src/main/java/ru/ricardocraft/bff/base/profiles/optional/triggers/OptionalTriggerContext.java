package ru.ricardocraft.bff.base.profiles.optional.triggers;

import ru.ricardocraft.bff.base.ClientPermissions;
import ru.ricardocraft.bff.base.profiles.ClientProfile;
import ru.ricardocraft.bff.base.profiles.PlayerProfile;
import ru.ricardocraft.bff.helper.JavaHelper;

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
