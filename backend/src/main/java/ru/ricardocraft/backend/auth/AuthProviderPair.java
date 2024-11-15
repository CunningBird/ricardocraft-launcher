package ru.ricardocraft.backend.auth;

import lombok.Getter;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.mix.MixProvider;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AuthProviderPair {
    public boolean isDefault = true;
    public AuthCoreProvider core;
    public TextureProvider textureProvider;
    public Map<String, MixProvider> mixes;
    public Map<String, String> links;
    public transient String name;
    @Getter
    public transient Set<String> features;
    public String displayName;
    public boolean visible = true;

    public AuthProviderPair() {
    }

    public AuthProviderPair(AuthCoreProvider core, TextureProvider textureProvider) {
        this.core = core;
        this.textureProvider = textureProvider;
    }

    public static void getFeatures(Class<?> clazz, Set<String> list) {
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
        T result = core.isSupport(clazz);
        if (result == null && mixes != null) {
            for(var m : mixes.values()) {
                result = m.isSupport(clazz);
                if(result != null) {
                    break;
                }
            }
        }
        return result;
    }

    public void init(AuthManager authManager,
                     LaunchServerConfig config,
                     AuthProviders authProviders,
                     KeyAgreementManager keyAgreementManager, String name) {
        this.name = name;
        if (links != null) link(authProviders);
        core.init(authManager, config, keyAgreementManager, this);
        features = new HashSet<>();
        getFeatures(core.getClass(), features);
        if(mixes != null) {
            for(var m : mixes.values()) {
                m.init(core);
                getFeatures(m.getClass(), features);
            }
        }
    }

    public void link(AuthProviders authProviders) {
        links.forEach((k, v) -> {
            AuthProviderPair pair = authProviders.getAuthProviderPair(v);
            if (pair == null) {
                throw new NullPointerException("Auth %s link failed. Pair %s not found".formatted(name, v));
            }
            if ("core".equals(k)) {
                if (pair.core == null)
                    throw new NullPointerException("Auth %s link failed. %s.core is null".formatted(name, v));
                core = pair.core;
            }
        });
    }

    public void close() throws IOException {
        core.close();
        if (textureProvider != null) {
            textureProvider.close();
        }
        if(mixes != null) {
            for(var m : mixes.values()) {
                m.close();
            }
        }
    }
}
