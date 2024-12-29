package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetAvailabilityAuthController extends AbstractController {

    private final AuthProviders authProviders;

    @Autowired
    public GetAvailabilityAuthController(ServerWebSocketHandler handler, AuthProviders authProviders) {
        super(GetAvailabilityAuthRequest.class, handler);
        this.authProviders = authProviders;
    }

    @Override
    public GetAvailabilityAuthResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        List<GetAvailabilityAuthResponse.AuthAvailability> list = new ArrayList<>();
        for (AuthProviderPair pair : authProviders.getAuthProviders().values()) {
            list.add(new GetAvailabilityAuthResponse.AuthAvailability(pair.core.getDetails(client), pair.name, pair.displayName,
                    pair.visible, pair.getFeatures()));
        }
        return new GetAvailabilityAuthResponse(list);
    }
}
