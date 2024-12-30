package ru.ricardocraft.backend.controller.update;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.update.UpdateRequest;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;
import ru.ricardocraft.backend.service.controller.update.UpdateRequestController;

@Component
public class UpdateController extends AbstractController {

    private final UpdateRequestController updateRequestController;

    public UpdateController(ServerWebSocketHandler handler,UpdateRequestController updateRequestController) {
        super(UpdateRequest.class, handler);
        this.updateRequestController = updateRequestController;
    }

    @Override
    public UpdateResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return updateRequestController.update(castRequest(request), client);
    }
}
