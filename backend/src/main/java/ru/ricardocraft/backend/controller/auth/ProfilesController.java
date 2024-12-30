package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.ProfilesRequest;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.service.controller.auth.ProfilesRequestService;

@Component
public class ProfilesController extends AbstractController {

    private final ProfilesRequestService profilesRequestService;

    public ProfilesController(ServerWebSocketHandler handler, ProfilesRequestService profilesRequestService) {
        super(ProfilesRequest.class, handler);
        this.profilesRequestService = profilesRequestService;
    }

    @Override
    public ProfilesResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return profilesRequestService.profiles(client);
    }
}
