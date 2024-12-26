package ru.ricardocraft.client.profiles.optional.triggers;

import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.profiles.PlayerProfile;
import ru.ricardocraft.client.utils.helper.JavaHelper;

public interface OptionalTriggerContext {
    ClientProfile getProfile();

    JavaHelper.JavaVersion getJavaVersion();

    default PlayerProfile getPlayerProfile() {
        return null;
    }
}
