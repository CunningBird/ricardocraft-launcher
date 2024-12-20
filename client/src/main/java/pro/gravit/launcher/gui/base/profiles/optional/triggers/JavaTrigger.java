package pro.gravit.launcher.gui.base.profiles.optional.triggers;

import pro.gravit.launcher.gui.base.profiles.optional.OptionalFile;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTrigger;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTriggerContext;
import pro.gravit.launcher.gui.utils.helper.JavaHelper;

public class JavaTrigger extends OptionalTrigger {
    public int minVersion;
    public int maxVersion;
    public boolean requireJavaFX;

    public JavaTrigger(int minVersion, int maxVersion, boolean requireJavaFX) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.requireJavaFX = requireJavaFX;
    }

    public JavaTrigger(int minVersion, int maxVersion) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
        this.requireJavaFX = false;
    }

    public JavaTrigger() {
        this.minVersion = 8;
        this.maxVersion = 999;
        this.requireJavaFX = false;
    }

    @Override
    public boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        JavaHelper.JavaVersion version = context.getJavaVersion();
        if (version.version < minVersion) return false;
        if (version.version > maxVersion) return false;
        return !requireJavaFX || version.enabledJavaFX;
    }
}
