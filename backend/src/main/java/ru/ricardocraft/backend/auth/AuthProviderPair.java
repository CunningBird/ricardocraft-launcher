package ru.ricardocraft.backend.auth;

import lombok.Getter;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.texture.TextureProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class AuthProviderPair {
    public boolean isDefault = true;
    public AuthCoreProvider core;
    public TextureProvider textureProvider;
    public transient String name;
    @Getter
    public transient Set<String> features;
    public String displayName;
    public boolean visible = true;

    public AuthProviderPair(AuthCoreProvider core,
                            TextureProvider textureProvider,
                            String name) {
        this.core = core;
        this.textureProvider = textureProvider;
        this.name = name;
        features = new HashSet<>();
        getFeatures(core.getClass(), features);
    }

    public void getFeatures(Class<?> clazz, Set<String> list) {
        Feature[] features = clazz.getAnnotationsByType(Feature.class);
        for (Feature feature : features) {
            list.add(feature.value());
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            getFeatures(superClass, list);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> i : interfaces) {
            getFeatures(i, list);
        }
    }

    public <T> T isSupport(Class<T> clazz) {
        if (core == null) return null;
        return core.isSupport(clazz);
    }

    public void close() throws IOException {
        core.close();
        if (textureProvider != null) {
            textureProvider.close();
        }
    }
}
