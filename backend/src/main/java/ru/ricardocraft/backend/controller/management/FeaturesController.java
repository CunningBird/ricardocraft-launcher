package ru.ricardocraft.backend.controller.management;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.FeaturesRequest;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.service.controller.management.FeaturesRequestService;

@Component
public class FeaturesController extends AbstractController {

    private final FeaturesRequestService featuresRequestService;

    public FeaturesController(ServerWebSocketHandler handler, FeaturesRequestService featuresRequestService) {
        super(FeaturesRequest.class, handler);
        this.featuresRequestService = featuresRequestService;
    }

    @Override
    public FeaturesResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return featuresRequestService.features();
    }
}
