package ru.ricardocraft.backend.controller.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.FetchClientProfileKeyRequest;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.service.controller.auth.FetchClientProfileKeyService;

@Component
public class FetchClientProfileKeyController extends AbstractController {

    private final FetchClientProfileKeyService fetchClientProfileKeyService;

    public FetchClientProfileKeyController(ServerWebSocketHandler handler, FetchClientProfileKeyService fetchClientProfileKeyService) {
        super(FetchClientProfileKeyRequest.class, handler);
        this.fetchClientProfileKeyService = fetchClientProfileKeyService;
    }

    @Override
    public FetchClientProfileKeyResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return fetchClientProfileKeyService.fetchClientProfileKey(client);
    }
}
