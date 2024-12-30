package ru.ricardocraft.backend.service.controller.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUsername;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUsernameResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

@Component
@RequiredArgsConstructor
public class ProfileByUsernameService {

    private final AuthProviders authProviders;
    private final AuthService authService;

    public ProfileByUsernameResponse profileByUsername(ProfileByUsername request, Client client) throws Exception {
        AuthProviderPair pair = client.auth;
        if (pair == null) pair = authProviders.getAuthProviderPair();
        PlayerProfile profile = authService.getPlayerProfile(pair, request.username);
        if (profile == null) {
            throw new Exception("User not found");
        }

        return new ProfileByUsernameResponse(profile);
    }
}
