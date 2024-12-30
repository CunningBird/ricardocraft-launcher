package ru.ricardocraft.backend.service.command.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenServerService {

    private final AuthService authService;
    private final ProfileProvider profileProvider;
    private final AuthProviders authProviders;

    public void tokenServer(String profileName, @Nullable String authId, Boolean publicOnly) {
        AuthProviderPair pair = (authId != null) ? authProviders.getAuthProviderPair(authId) : authProviders.getAuthProviderPair();
        ClientProfile profile = null;
        for (ClientProfile p : profileProvider.getProfiles()) {
            if (p.getTitle().equals(profileName) || p.getUUID().toString().equals(profileName)) {
                profile = p;
                break;
            }
        }
        if (profile == null) {
            log.warn("Profile {} not found", profileName);
        }
        if (pair == null) {
            log.error("AuthId {} not found", authId);
            return;
        }
        String token = authService.newCheckServerToken(profile != null ? profile.getUUID().toString() : profileName, pair.name, publicOnly);
        log.info("Server token {} authId {}: {}", profileName, pair.name, token);
    }
}
