package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.SetProfileRequest;
import ru.ricardocraft.backend.dto.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class SetProfileService {

    private final ProfileProvider profileProvider;
    private final ProtectHandler protectHandler;

    public SetProfileResponse setProfile(SetProfileRequest request, Client client) throws Exception {
        Collection<ClientProfile> profiles = profileProvider.getProfiles();
        for (ClientProfile p : profiles) {
            if (p.getTitle().equals(request.client)) {
                if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler &&
                        !profilesProtectHandler.canChangeProfile(p, client)) {
                    throw new Exception("Access denied");
                }
                client.profile = p;
                return new SetProfileResponse(p);
            }
        }
        throw new Exception("Profile not found");
    }
}
