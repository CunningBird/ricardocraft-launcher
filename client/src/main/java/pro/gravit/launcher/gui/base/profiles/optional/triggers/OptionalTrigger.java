package pro.gravit.launcher.gui.base.profiles.optional.triggers;

import pro.gravit.launcher.gui.base.profiles.optional.OptionalFile;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.ArchTrigger;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.JavaTrigger;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OSTrigger;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTriggerContext;
import pro.gravit.launcher.gui.utils.ProviderMap;

public abstract class OptionalTrigger {
    public static ProviderMap<OptionalTrigger> providers = new ProviderMap<>("OptionalTriggers");
    private static boolean isRegisteredProviders = false;
    public boolean required;
    public boolean inverted;

    public static void registerProviders() {
        if (!isRegisteredProviders) {
            providers.register("java", JavaTrigger.class);
            providers.register("os", OSTrigger.class);
            providers.register("arch", ArchTrigger.class);
            isRegisteredProviders = true;
        }
    }

    protected abstract boolean isTriggered(OptionalFile optional, pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTriggerContext context);

    public boolean check(OptionalFile optional, OptionalTriggerContext context) {
        boolean result = isTriggered(optional, context);
        if (inverted) result = !result;
        return result;
    }
}
