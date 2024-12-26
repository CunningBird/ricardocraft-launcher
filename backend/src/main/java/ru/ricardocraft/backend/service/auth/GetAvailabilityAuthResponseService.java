package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.auth.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetAvailabilityAuthResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    @Autowired
    public GetAvailabilityAuthResponseService(WebSocketService service, AuthProviders authProviders) {
        super(GetAvailabilityAuthResponse.class, service);
        this.authProviders = authProviders;
    }

    @Override
    public GetAvailabilityAuthRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        List<GetAvailabilityAuthRequestEvent.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            list.add(new GetAvailabilityAuthRequestEvent.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        return new GetAvailabilityAuthRequestEvent(list);
    }
}
