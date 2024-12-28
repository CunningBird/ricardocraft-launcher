package ru.ricardocraft.backend.service.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUUIDRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class ProfileByUUIDService extends AbstractService {

    private final AuthProviders authProviders;
    private final AuthManager authManager;

    @Autowired
    public ProfileByUUIDService(ServerWebSocketHandler handler,
                                AuthProviders authProviders,
                                AuthManager authManager) {
        super(ProfileByUUIDRequest.class, handler);
        this.authProviders = authProviders;
        this.authManager = authManager;
    }

    @Override
    public ProfileByUUIDResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        ProfileByUUIDRequest response = (ProfileByUUIDRequest) rawResponse;

        AuthProviderPair pair;
        if (client.auth == null) {
            pair = authProviders.getAuthProviderPair();
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("ProfileByUUIDRequest: AuthProviderPair is null");
        }
        User user = pair.core.getUserByUUID(response.uuid);
        if (user == null) {
            throw new Exception("User not found");
        }
        return new ProfileByUUIDResponse(authManager.getPlayerProfile(pair, response.uuid));
    }
}
