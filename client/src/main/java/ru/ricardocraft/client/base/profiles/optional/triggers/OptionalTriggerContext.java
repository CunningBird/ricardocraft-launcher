package ru.ricardocraft.client.base.profiles.optional.triggers;

import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.utils.helper.JavaHelper;

public interface OptionalTriggerContext {
    ClientProfile getProfile();

    JavaHelper.JavaVersion getJavaVersion();

    default PlayerProfile getPlayerProfile() {
        return null;
    }
}
