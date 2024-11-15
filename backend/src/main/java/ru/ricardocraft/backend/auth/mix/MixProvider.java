package ru.ricardocraft.backend.auth.mix;

import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.utils.ProviderMap;

public abstract class MixProvider implements  AutoCloseable{
    public static final ProviderMap<MixProvider> providers = new ProviderMap<>("MixProvider");
    private static boolean registredProviders = false;

    public static void registerProviders() {
        if (!registredProviders) {
            providers.register("uploadAsset", UploadAssetMixProvider.class);
            registredProviders = true;
        }
    }

    public abstract void init(AuthCoreProvider core);

    @SuppressWarnings("unchecked")
    public <T> T isSupport(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) return (T) this;
        return null;
    }

    @Override
    public abstract void close();
}
