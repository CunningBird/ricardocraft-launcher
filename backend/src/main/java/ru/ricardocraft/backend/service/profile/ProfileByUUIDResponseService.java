package ru.ricardocraft.backend.service.profile;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.profile.ProfileByUUIDRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class ProfileByUUIDResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public ProfileByUUIDResponseService(ServerWebSocketHandler handler,
                                        AuthProviders authProviders,
                                        AuthManager authManager) {
        super(ProfileByUUIDResponse.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public ProfileByUUIDRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        ProfileByUUIDResponse response = (ProfileByUUIDResponse) rawResponse;

        AuthProviderPair pair;
        if (client.auth == null) {
            pair = authProviders.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("ProfileByUUIDResponse: AuthProviderPair is null");
        }
        User user = pair.core.getUserByUUID(response.uuid);
        if (user == null) {
            throw new Exception("User not found");
        }
        return new ProfileByUUIDRequestEvent(authManager.getPlayerProfile(pair, response.uuid));
    }
}
