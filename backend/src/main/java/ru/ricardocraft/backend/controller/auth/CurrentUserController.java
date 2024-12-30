package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CurrentUserRequest;
import ru.ricardocraft.backend.dto.response.auth.CurrentUserResponse;
import ru.ricardocraft.backend.service.controller.auth.CurrentUserService;

@Component
public class CurrentUserController extends AbstractController {

    private final CurrentUserService currentUserService;

    public CurrentUserController(ServerWebSocketHandler handler, CurrentUserService currentUserService) {
        super(CurrentUserRequest.class, handler);
        this.currentUserService = currentUserService;
    }

    @Override
    public CurrentUserResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return currentUserService.getCurrentUser(client);
    }
}
