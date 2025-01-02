package ru.ricardocraft.client.service.profiles.optional.triggers;

import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.service.profiles.PlayerProfile;
import ru.ricardocraft.client.base.helper.JavaHelper;

public interface OptionalTriggerContext {
    ClientProfile getProfile();

    JavaHelper.JavaVersion getJavaVersion();

    default PlayerProfile getPlayerProfile() {
        return null;
    }
}
