package ru.ricardocraft.backend.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.GetConnectUUIDRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetConnectUUIDResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getConnectUUID";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        sendResult(new GetConnectUUIDRequestEvent(connectUUID, shardId));
    }
}
