package ru.ricardocraft.backend.controller.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUUIDRequest;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class ProfileByUUIDController extends AbstractController {

    private final AuthProviders authProviders;
    private final AuthService authService;

    @Autowired
    public ProfileByUUIDController(ServerWebSocketHandler handler,
                                   AuthProviders authProviders,
                                   AuthService authService) {
        super(ProfileByUUIDRequest.class, handler);
        this.authProviders = authProviders;
        this.authService = authService;
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
        return new ProfileByUUIDResponse(authService.getPlayerProfile(pair, response.uuid));
    }
}
