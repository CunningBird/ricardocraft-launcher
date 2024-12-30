package ru.ricardocraft.backend.controller.update;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.update.LauncherRequest;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.service.controller.update.LauncherRequestService;

@Component
public class LauncherController extends AbstractController {

    private final LauncherRequestService launcherRequestService;

    public LauncherController(ServerWebSocketHandler handler, LauncherRequestService launcherRequestService) {
        super(LauncherRequest.class, handler);
        this.launcherRequestService = launcherRequestService;
    }

    @Override
    public LauncherResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return launcherRequestService.launcher(castRequest(request), client);
    }
}
