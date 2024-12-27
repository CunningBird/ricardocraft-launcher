package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.ProfilesRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class ProfilesResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    private final ProfileProvider profileProvider;

    @Autowired
    public ProfilesResponseService(ServerWebSocketHandler handler, ProtectHandler protectHandler, ProfileProvider profileProvider) {
        super(ProfilesResponse.class, handler);
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
    }

    @Override
    public ProfilesRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetProfiles(client)) {
            throw new Exception("Access denied");
        }

        return new ProfilesRequestEvent(profileProvider.getProfiles(client));
    }
}
