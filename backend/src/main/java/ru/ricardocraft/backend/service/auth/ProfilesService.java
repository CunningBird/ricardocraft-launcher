package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.ProfilesRequest;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class ProfilesService extends AbstractService {

    private final ProtectHandler protectHandler;

    private final ProfileProvider profileProvider;

    @Autowired
    public ProfilesService(ServerWebSocketHandler handler, ProtectHandler protectHandler, ProfileProvider profileProvider) {
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
