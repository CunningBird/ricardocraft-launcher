package ru.ricardocraft.backend.service.profile;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.profile.ProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.profiles.PlayerProfile;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUsername;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class ProfileByUsernameService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public ProfileByUsernameService(WebSocketService service,
                                    AuthProviders authProviders,
                                    AuthManager authManager) {
        super(ProfileByUsername.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public ProfileByUsernameRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        ProfileByUsername response = (ProfileByUsername) rawResponse;

        AuthProviderPair pair = client.auth;
        if (pair == null) pair = authProviders.getAuthProviderPair();
        PlayerProfile profile = authManager.getPlayerProfile(pair, response.username);
        if (profile == null) {
            throw new Exception("User not found");
        }

        return new ProfileByUsernameRequestEvent(profile);
    }
}
