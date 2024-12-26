package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.ProfilesRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class ProfilesResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    private final ProfileProvider profileProvider;

    @Autowired
    public ProfilesResponseService(WebSocketService service,
                                   ProtectHandler protectHandler,
                                   ProfileProvider profileProvider) {
        super(ProfilesResponse.class, service);
        this.protectHandler = protectHandler;
        this.profileProvider = profileProvider;
    }

    @Override
    public ProfilesRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetProfiles(client)) {
            throw new Exception("Access denied");
        }

        return new ProfilesRequestEvent(profileProvider.getProfiles(client));
    }
}
