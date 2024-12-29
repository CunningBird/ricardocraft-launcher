package ru.ricardocraft.backend.controller.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.profile.BatchProfileByUsernameResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class BatchProfileByUsernameController extends AbstractController {

    private final AuthProviders authProviders;
    private final AuthService authService;

    @Autowired
    public BatchProfileByUsernameController(ServerWebSocketHandler handler,
                                            AuthProviders authProviders,
                                            AuthService authService) {
        super(BatchProfileByUsername.class, handler);
        this.authProviders = authProviders;
        this.authService = authService;
    }

    @Override
    public BatchProfileByUsernameResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        BatchProfileByUsername response = (BatchProfileByUsername) rawResponse;

        BatchProfileByUsernameResponse result = new BatchProfileByUsernameResponse();
        if (response.list == null) {
            throw new Exception("Invalid request");
        }
        result.playerProfiles = new PlayerProfile[response.list.length];
        for (int i = 0; i < response.list.length; ++i) {
            AuthProviderPair pair = client.auth;
            if (pair == null) {
                pair = authProviders.getAuthProviderPair();
            }
            result.playerProfiles[i] = authService.getPlayerProfile(pair, response.list[i].username);
        }
        return result;
    }
}
