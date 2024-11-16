package ru.ricardocraft.backend.auth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.texture.TextureProvider;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthProviders {

    @Getter
    private final Map<String, AuthProviderPair> authProviders = new HashMap<>();

    private transient AuthProviderPair authDefault;

    @Autowired
    public AuthProviders(@Qualifier("memoryAuthCoreProvider") AuthCoreProvider provider,
                         @Qualifier("requestTextureProvider") TextureProvider requestTextureProvider) {
        String providerName = "std";
        AuthProviderPair a = new AuthProviderPair(provider, requestTextureProvider, providerName);
        a.displayName = "Default";
        authProviders.put(providerName, a);

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
