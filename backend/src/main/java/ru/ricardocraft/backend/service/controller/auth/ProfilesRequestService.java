package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;

@Component
@RequiredArgsConstructor
public class ProfilesRequestService {

    private final ProtectHandler protectHandler;
    private final ProfileProvider profileProvider;

    public ProfilesResponse profiles(Client client) throws Exception {
        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetProfiles(client)) {
            throw new Exception("Access denied");
        }

        return new ProfilesResponse(profileProvider.getProfiles(client));
    }
}
