package ru.ricardocraft.backend.controller.profile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUsernameResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUsername;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.profiles.PlayerProfile;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class ProfileByUsernameController extends AbstractController {

    private final AuthProviders authProviders;
    private final AuthService authService;

    @Autowired
    public ProfileByUsernameController(ServerWebSocketHandler handler,
                                       AuthProviders authProviders,
                                       AuthService authService) {
        super(ProfileByUsername.class, handler);
        this.authProviders = authProviders;
        this.authService = authService;
    }

    @Override
    public ProfileByUsernameResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        ProfileByUsername response = (ProfileByUsername) rawResponse;

        AuthProviderPair pair = client.auth;
        if (pair == null) pair = authProviders.getAuthProviderPair();
        PlayerProfile profile = authService.getPlayerProfile(pair, response.username);
        if (profile == null) {
            throw new Exception("User not found");
        }

        return new ProfileByUsernameResponse(profile);
    }
}
