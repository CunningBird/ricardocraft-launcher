package ru.ricardocraft.backend.service.controller.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUUIDRequest;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;

@Component
@RequiredArgsConstructor
public class ProfileByUuidService {

    private final AuthProviders authProviders;
    private final AuthService authService;

    public ProfileByUUIDResponse profileByUuid(ProfileByUUIDRequest request, Client client) throws Exception {
        AuthProviderPair pair;
        if (client.auth == null) {
            pair = authProviders.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("ProfileByUUIDRequest: AuthProviderPair is null");
        }
        User user = pair.core.getUserByUUID(request.uuid);
        if (user == null) {
            throw new Exception("User not found");
        }
        return new ProfileByUUIDResponse(authService.getPlayerProfile(pair, request.uuid));
    }
}
