package ru.ricardocraft.backend.service.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.FeaturesRequest;
import ru.ricardocraft.backend.manangers.FeaturesManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class FeaturesService extends AbstractService {

    private final FeaturesManager featuresManager;

    @Autowired
    public FeaturesService(ServerWebSocketHandler handler, FeaturesManager featuresManager) {
        super(FeaturesRequest.class, handler);
        this.featuresManager = featuresManager;
    }

    @Override
    public FeaturesResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        return new FeaturesResponse(featuresManager.getMap());
    }
}
