package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.auth.GetAvailabilityAuthResponse;
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
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        GetAvailabilityAuthResponse response = (GetAvailabilityAuthResponse) rawResponse;

        List<GetAvailabilityAuthRequestEvent.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            list.add(new GetAvailabilityAuthRequestEvent.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        sendResult(ctx, new GetAvailabilityAuthRequestEvent(list), response.requestUUID);
    }
}
