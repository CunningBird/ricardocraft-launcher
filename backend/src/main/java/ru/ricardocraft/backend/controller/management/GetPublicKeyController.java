package ru.ricardocraft.backend.controller.management;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.GetPublicKeyRequest;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.service.controller.management.GetPublicKeyService;

@Component
public class GetPublicKeyController extends AbstractController {

    private final GetPublicKeyService getPublicKeyService;

    public GetPublicKeyController(ServerWebSocketHandler handler, GetPublicKeyService getPublicKeyService) {
        super(GetPublicKeyRequest.class, handler);
        this.getPublicKeyService = getPublicKeyService;
    }

    @Override
    public GetPublicKeyResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return getPublicKeyService.getPublicKey();
    }
}
