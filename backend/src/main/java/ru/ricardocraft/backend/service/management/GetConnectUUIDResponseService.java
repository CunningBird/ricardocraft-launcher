package ru.ricardocraft.backend.service.management;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.request.management.GetConnectUUIDRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class GetConnectUUIDResponseService extends AbstractResponseService {

    private final int shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));

    @Autowired
    public GetConnectUUIDResponseService(ServerWebSocketHandler handler) {
        super(GetConnectUUIDResponse.class, handler);
    }

    @Override
    public GetConnectUUIDRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        GetConnectUUIDResponse response = (GetConnectUUIDResponse) rawResponse;
        return new GetConnectUUIDRequestEvent(response.connectUUID, shardId);
    }
}
