package ru.ricardocraft.backend.controller.profile;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.ProfileByUUIDRequest;
import ru.ricardocraft.backend.dto.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.service.controller.profile.ProfileByUuidService;

@Component
public class ProfileByUUIDController extends AbstractController {

    private final ProfileByUuidService profileByUuidService;

    public ProfileByUUIDController(ServerWebSocketHandler handler, ProfileByUuidService profileByUuidService) {
        super(ProfileByUUIDRequest.class, handler);
        this.profileByUuidService = profileByUuidService;
    }

    @Override
    public ProfileByUUIDResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return profileByUuidService.profileByUuid(castRequest(request), client);
    }
}
