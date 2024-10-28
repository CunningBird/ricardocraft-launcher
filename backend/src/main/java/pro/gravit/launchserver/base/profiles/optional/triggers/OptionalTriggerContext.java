package pro.gravit.launchserver.base.profiles.optional.triggers;

import pro.gravit.launchserver.base.ClientPermissions;
import pro.gravit.launchserver.base.profiles.ClientProfile;
import pro.gravit.launchserver.base.profiles.PlayerProfile;
import pro.gravit.launchserver.utils.helper.JavaHelper;

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
