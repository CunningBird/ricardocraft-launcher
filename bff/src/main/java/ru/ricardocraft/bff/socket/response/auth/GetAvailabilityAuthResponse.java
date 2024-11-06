package ru.ricardocraft.bff.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.bff.auth.AuthProviderPair;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

public class GetAvailabilityAuthResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getAvailabilityAuth";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        List<GetAvailabilityAuthRequestEvent.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : server.config.auth.values()) {
            list.add(new GetAvailabilityAuthRequestEvent.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        sendResult(new GetAvailabilityAuthRequestEvent(list));
    }
}
