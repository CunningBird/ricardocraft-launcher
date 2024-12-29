package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.ProfilesRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class ProfilesController extends AbstractController {

    private final ProtectHandler protectHandler;

    private final ProfileProvider profileProvider;

    @Autowired
    public ProfilesController(ServerWebSocketHandler handler, ProtectHandler protectHandler, ProfileProvider profileProvider) {
        super(ProfilesRequest.class, handler);
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
    }

    @Override
    public ProfilesResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetProfiles(client)) {
            throw new Exception("Access denied");
        }

        return new ProfilesResponse(profileProvider.getProfiles(client));
    }
}
