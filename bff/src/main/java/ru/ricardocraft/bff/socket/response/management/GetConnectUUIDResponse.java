package ru.ricardocraft.bff.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.GetConnectUUIDRequestEvent;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

public class GetConnectUUIDResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getConnectUUID";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        sendResult(new GetConnectUUIDRequestEvent(connectUUID, server.shardId));
    }
}
