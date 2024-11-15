package ru.ricardocraft.backend.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.MemoryAuthCoreProvider;
import ru.ricardocraft.backend.auth.texture.RequestTextureProvider;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthProviders {

    @Getter
    Map<String, AuthProviderPair> authProviders = new HashMap<>();

    private transient AuthProviderPair authDefault;

    @Autowired
    public AuthProviders(LaunchServerConfig config, AuthManager authManager, KeyAgreementManager keyAgreementManager) {
        AuthProviderPair a = new AuthProviderPair(
                new MemoryAuthCoreProvider(),
                new RequestTextureProvider(config.textureProvider.skinURL, config.textureProvider.cloakURL)
        );
        a.displayName = "Default";
        authProviders.put("std", a);

        for (Map.Entry<String, AuthProviderPair> provider : authProviders.entrySet()) {
            provider.getValue().init(authManager, config, this, keyAgreementManager, provider.getKey());
        }

        if (authProviders == null || authProviders.isEmpty()) {
            throw new NullPointerException("AuthProviderPair`s count should be at least one");
        }

        boolean isOneDefault = false;
        for (AuthProviderPair pair : authProviders.values()) {
            if (pair.isDefault) {
                isOneDefault = true;
                break;
            }
        }
        if (!isOneDefault) {
            throw new IllegalStateException("No auth pairs declared by default.");
        }
    }

    public AuthProviderPair getAuthProviderPair(String name) {
        return authProviders.get(name);
    }

    public AuthProviderPair getAuthProviderPair() {
        if (authDefault != null) return authDefault;
        for (AuthProviderPair pair : authProviders.values()) {
            if (pair.isDefault) {
                authDefault = pair;
                return pair;
            }
        }
        throw new IllegalStateException("Default AuthProviderPair not found");
    }
}
