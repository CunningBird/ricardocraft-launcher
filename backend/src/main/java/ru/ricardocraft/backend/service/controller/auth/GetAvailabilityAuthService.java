package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAvailabilityAuthService {

    private final AuthProviders authProviders;

    public GetAvailabilityAuthResponse getAvailabilityAuth(Client client) {
        List<GetAvailabilityAuthResponse.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            list.add(new GetAvailabilityAuthResponse.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        return new GetAvailabilityAuthResponse(list);
    }
}
