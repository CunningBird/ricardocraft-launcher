package ru.ricardocraft.backend.controller.profile;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUsername;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUsernameResponse;
import ru.ricardocraft.backend.service.controller.profile.ProfileByUsernameService;

@Component
public class ProfileByUsernameController extends AbstractController {

    private final ProfileByUsernameService profileByUsernameService;

    public ProfileByUsernameController(ServerWebSocketHandler handler, ProfileByUsernameService profileByUsernameService) {
        super(ProfileByUsername.class, handler);
        this.profileByUsernameService = profileByUsernameService;
    }

    @Override
    public ProfileByUsernameResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return profileByUsernameService.profileByUsername(castRequest(request), client);
    }
}
