package ru.ricardocraft.backend.service.profile;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.BatchProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.dto.socket.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class BatchProfileByUsernameService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public BatchProfileByUsernameService(WebSocketService service,
                                         AuthProviders authProviders,
                                         AuthManager authManager) {
        super(BatchProfileByUsername.class, service);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        BatchProfileByUsername response = (BatchProfileByUsername) rawResponse;

        BatchProfileByUsernameRequestEvent result = new BatchProfileByUsernameRequestEvent();
        if (response.list == null) {
            sendError(ctx, "Invalid request", response.requestUUID);
            return;
        }
        result.playerProfiles = new PlayerProfile[response.list.length];
        for (int i = 0; i < response.list.length; ++i) {
            AuthProviderPair pair = client.auth;
            if (pair == null) {
                pair = authProviders.getAuthProviderPair();
            }
            result.playerProfiles[i] = authManager.getPlayerProfile(pair, response.list[i].username);
        }
        sendResult(ctx, result, response.requestUUID);
    }
}
