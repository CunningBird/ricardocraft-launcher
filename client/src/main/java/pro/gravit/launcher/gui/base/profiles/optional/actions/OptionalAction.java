package pro.gravit.launcher.gui.base.profiles.optional.actions;

import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalActionClassPath;
import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalActionClientArgs;
import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalActionFile;
import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalActionJvmArgs;
import pro.gravit.launcher.gui.utils.ProviderMap;

public class OptionalAction {
    public static final ProviderMap<OptionalAction> providers = new ProviderMap<>();
    private static boolean registerProviders = false;

    public static void registerProviders() {
        if (!registerProviders) {
            providers.register("file", OptionalActionFile.class);
            providers.register("clientArgs", OptionalActionClientArgs.class);
            providers.register("jvmArgs", OptionalActionJvmArgs.class);
            providers.register("classpath", OptionalActionClassPath.class);
            registerProviders = true;
        }
    }
}
