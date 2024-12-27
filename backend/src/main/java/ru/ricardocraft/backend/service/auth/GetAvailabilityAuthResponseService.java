package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.dto.events.request.auth.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetAvailabilityAuthResponseService extends AbstractResponseService {

    private final AuthProviders authProviders;

    @Autowired
    public GetAvailabilityAuthResponseService(ServerWebSocketHandler handler, AuthProviders authProviders) {
        super(GetAvailabilityAuthResponse.class, handler);
        this.authProviders = authProviders;
    }

    @Override
    public GetAvailabilityAuthRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        List<GetAvailabilityAuthRequestEvent.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            list.add(new GetAvailabilityAuthRequestEvent.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        return new GetAvailabilityAuthRequestEvent(list);
    }
}
