package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.SetProfileRequest;
import ru.ricardocraft.backend.dto.response.auth.SetProfileResponse;
import ru.ricardocraft.backend.service.controller.auth.SetProfileService;

@Component
public class SetProfileController extends AbstractController {

    private final SetProfileService setProfileService;

    public SetProfileController(ServerWebSocketHandler handler, SetProfileService setProfileService) {
        super(SetProfileRequest.class, handler);
        this.setProfileService = setProfileService;
    }

    @Override
    public SetProfileResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return setProfileService.setProfile(castRequest(request), client);
    }
}
