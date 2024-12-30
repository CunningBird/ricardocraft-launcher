package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AdditionalDataRequest;
import ru.ricardocraft.backend.dto.response.auth.AdditionalDataResponse;
import ru.ricardocraft.backend.service.controller.auth.AdditionalDataService;

@Component
public class AdditionalDataController extends AbstractController {

    private final AdditionalDataService additionalDataService;

    protected AdditionalDataController(ServerWebSocketHandler handler, AdditionalDataService additionalDataService) {
        super(AdditionalDataRequest.class, handler);
        this.additionalDataService = additionalDataService;
    }

    @Override
    public AdditionalDataResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return additionalDataService.getAdditionalData(castRequest(request), client);
    }
}
