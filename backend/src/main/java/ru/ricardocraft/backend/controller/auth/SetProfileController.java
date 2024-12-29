package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.SetProfileRequest;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

import java.util.Collection;

@Component
public class SetProfileController extends AbstractController {

    private final ProfileProvider profileProvider;
    private final ProtectHandler protectHandler;

    @Autowired
    public SetProfileController(ServerWebSocketHandler handler,
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
