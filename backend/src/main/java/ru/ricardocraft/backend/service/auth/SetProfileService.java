package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.SetProfileRequest;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.util.Collection;

@Component
public class SetProfileService extends AbstractService {

    private final ProfileProvider profileProvider;
    private final ProtectHandler protectHandler;

    @Autowired
    public SetProfileService(ServerWebSocketHandler handler,
                             ProfileProvider profileProvider,
                             ProtectHandler protectHandler) {
        super(SetProfileRequest.class, handler);
        this.profileProvider = profileProvider;
        this.protectHandler = protectHandler;
    }

    @Override
    public SetProfileResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        SetProfileRequest response = (SetProfileRequest) rawResponse;
        Collection<ClientProfile> profiles = profileProvider.getProfiles();
        for (ClientProfile p : profiles) {
            if (p.getTitle().equals(response.client)) {
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
