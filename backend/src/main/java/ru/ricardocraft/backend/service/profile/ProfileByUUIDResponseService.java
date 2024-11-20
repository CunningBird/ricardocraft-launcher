package ru.ricardocraft.backend.service.profile;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.ProfileByUUIDRequestEvent;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class ProfileByUUIDResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public ProfileByUUIDResponseService(WebSocketService service,
                                        AuthProviders authProviders,
                                        AuthManager authManager) {
        super(ProfileByUUIDResponse.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        ProfileByUUIDResponse response = (ProfileByUUIDResponse) rawResponse;

        AuthProviderPair pair;
        if (client.auth == null) {
            pair = authProviders.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            sendError(ctx, "ProfileByUUIDResponse: AuthProviderPair is null", response.requestUUID);
            return;
        }
        User user = pair.core.getUserByUUID(response.uuid);
        if (user == null) {
            sendError(ctx, "User not found", response.requestUUID);
            return;
        }
        sendResult(ctx, new ProfileByUUIDRequestEvent(authManager.getPlayerProfile(pair, response.uuid)), response.requestUUID);
    }
}
