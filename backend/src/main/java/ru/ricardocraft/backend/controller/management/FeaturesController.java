package ru.ricardocraft.backend.controller.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.FeaturesRequest;
import ru.ricardocraft.backend.service.FeaturesService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class FeaturesController extends AbstractController {

    private final FeaturesService featuresService;

    @Autowired
    public FeaturesController(ServerWebSocketHandler handler, FeaturesService featuresService) {
        super(FeaturesRequest.class, handler);
        this.featuresService = featuresService;
    }

    @Override
    public FeaturesResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        return new FeaturesResponse(featuresService.getMap());
    }
}
