package ru.ricardocraft.backend.base.profiles.optional.triggers;

import ru.ricardocraft.backend.base.helper.JavaHelper;
import ru.ricardocraft.backend.base.profiles.optional.OptionalFile;

public class JavaTrigger extends OptionalTrigger {
    public int minVersion;
    public int maxVersion;
    public boolean requireJavaFX;

    @Override
    public boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        JavaHelper.JavaVersion version = context.getJavaVersion();
        if (version.version < minVersion) return false;
        if (version.version > maxVersion) return false;
        return !requireJavaFX || version.enabledJavaFX;
    }
}
