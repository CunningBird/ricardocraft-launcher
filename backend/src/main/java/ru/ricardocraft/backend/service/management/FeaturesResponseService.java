package ru.ricardocraft.backend.service.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.request.management.FeaturesRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.manangers.FeaturesManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class FeaturesResponseService extends AbstractResponseService {

    private final FeaturesManager featuresManager;

    @Autowired
    public FeaturesResponseService(ServerWebSocketHandler handler, FeaturesManager featuresManager) {
        super(FeaturesResponse.class, handler);
        this.featuresManager = featuresManager;
    }

    @Override
    public FeaturesRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        return new FeaturesRequestEvent(featuresManager.getMap());
    }
}
