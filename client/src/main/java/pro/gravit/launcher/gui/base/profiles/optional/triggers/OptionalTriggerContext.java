package pro.gravit.launcher.gui.base.profiles.optional.triggers;

import pro.gravit.launcher.gui.base.ClientPermissions;
import pro.gravit.launcher.gui.base.profiles.ClientProfile;
import pro.gravit.launcher.gui.base.profiles.PlayerProfile;
import pro.gravit.launcher.gui.utils.helper.JavaHelper;

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
