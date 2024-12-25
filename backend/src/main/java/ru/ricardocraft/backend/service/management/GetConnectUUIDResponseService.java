package ru.ricardocraft.backend.service.management;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.events.request.management.GetConnectUUIDRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class GetConnectUUIDResponseService extends AbstractResponseService {

    private final int shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));

    @Autowired
    public GetConnectUUIDResponseService(WebSocketService service) {
        super(GetConnectUUIDResponse.class, service);
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        GetConnectUUIDResponse response = (GetConnectUUIDResponse) rawResponse;

        sendResult(ctx, new GetConnectUUIDRequestEvent(response.connectUUID, shardId), response.requestUUID);
    }
}
