package ru.ricardocraft.backend.service.management;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.backend.manangers.FeaturesManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.management.FeaturesResponse;

@Component
public class FeaturesResponseService extends AbstractResponseService {

    private final FeaturesManager featuresManager;

    @Autowired
    public FeaturesResponseService(WebSocketService service, FeaturesManager featuresManager) {
        super(FeaturesResponse.class, service);
        this.featuresManager = featuresManager;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        FeaturesResponse response = (FeaturesResponse) rawResponse;
        sendResult(ctx, new FeaturesRequestEvent(featuresManager.getMap()), response.requestUUID);
    }
}
