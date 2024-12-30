package ru.ricardocraft.backend.controller.profile;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.dto.response.profile.BatchProfileByUsernameResponse;
import ru.ricardocraft.backend.service.controller.profile.BatchProfileByUsernameService;

@Component
public class BatchProfileByUsernameController extends AbstractController {

    private final BatchProfileByUsernameService batchProfileByUsernameService;

    public BatchProfileByUsernameController(ServerWebSocketHandler handler, BatchProfileByUsernameService batchProfileByUsernameService) {
        super(BatchProfileByUsername.class, handler);
        this.batchProfileByUsernameService = batchProfileByUsernameService;
    }

    @Override
    public BatchProfileByUsernameResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return batchProfileByUsernameService.batchProfileByUsername(castRequest(request), client);
    }
}
