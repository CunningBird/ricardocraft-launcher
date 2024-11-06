package ru.ricardocraft.bff.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

public class FeaturesResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "features";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        sendResult(new FeaturesRequestEvent(server.featuresManager.getMap()));
    }
}
