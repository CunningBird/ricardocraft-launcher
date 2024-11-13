package ru.ricardocraft.backend.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class FeaturesResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "features";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        sendResult(new FeaturesRequestEvent(featuresManager.getMap()));
    }
}
