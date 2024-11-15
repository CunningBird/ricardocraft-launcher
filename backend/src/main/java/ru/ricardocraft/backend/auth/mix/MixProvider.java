package ru.ricardocraft.backend.auth.mix;

import ru.ricardocraft.backend.auth.core.AuthCoreProvider;

public abstract class MixProvider implements AutoCloseable {

    public abstract void init(AuthCoreProvider core);

    @SuppressWarnings("unchecked")
    public <T> T isSupport(Class<T> clazz) {
        if (clazz.isAssignableFrom(getClass())) return (T) this;
        return null;
    }

    @Override
    public abstract void close();
}
