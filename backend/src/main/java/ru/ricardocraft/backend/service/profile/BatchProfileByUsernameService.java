package ru.ricardocraft.backend.service.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.profile.BatchProfileByUsernameRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.profiles.PlayerProfile;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class BatchProfileByUsernameService extends AbstractResponseService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public BatchProfileByUsernameService(ServerWebSocketHandler handler,
                                         AuthProviders authProviders,
                                         AuthManager authManager) {
        super(BatchProfileByUsername.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public BatchProfileByUsernameRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        BatchProfileByUsername response = (BatchProfileByUsername) rawResponse;

        BatchProfileByUsernameRequestEvent result = new BatchProfileByUsernameRequestEvent();
        if (response.list == null) {
            throw new Exception("Invalid request");
        }
        result.playerProfiles = new PlayerProfile[response.list.length];
        for (int i = 0; i < response.list.length; ++i) {
            AuthProviderPair pair = client.auth;
            if (pair == null) {
                pair = authProviders.getAuthProviderPair();
            }
            result.playerProfiles[i] = authManager.getPlayerProfile(pair, response.list[i].username);
        }
        return result;
    }
}
