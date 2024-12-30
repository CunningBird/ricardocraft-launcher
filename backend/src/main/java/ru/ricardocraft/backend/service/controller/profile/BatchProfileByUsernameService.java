package ru.ricardocraft.backend.service.controller.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.dto.response.profile.BatchProfileByUsernameResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;

@Component
@RequiredArgsConstructor
public class BatchProfileByUsernameService {

    private final AuthProviders authProviders;
    private final AuthService authService;

    public BatchProfileByUsernameResponse batchProfileByUsername(BatchProfileByUsername request, Client client) throws Exception {
        BatchProfileByUsernameResponse result = new BatchProfileByUsernameResponse();
        if (request.list == null) {
            throw new Exception("Invalid request");
        }
        result.playerProfiles = new PlayerProfile[request.list.length];
        for (int i = 0; i < request.list.length; ++i) {
            AuthProviderPair pair = client.auth;
            if (pair == null) {
                pair = authProviders.getAuthProviderPair();
            }
            result.playerProfiles[i] = authService.getPlayerProfile(pair, request.list[i].username);
        }
        return result;
    }
}
