package ru.ricardocraft.backend.controller.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.GetPublicKeyRequest;
import ru.ricardocraft.backend.service.KeyAgreementService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class GetPublicKeyController extends AbstractController {

    private final KeyAgreementService keyAgreementService;

    @Autowired
    public GetPublicKeyController(ServerWebSocketHandler handler, KeyAgreementService keyAgreementService) {
        super(GetPublicKeyRequest.class, handler);
        this.keyAgreementService = keyAgreementService;
    }

    @Override
    public GetPublicKeyResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        return new GetPublicKeyResponse(keyAgreementService.rsaPublicKey, keyAgreementService.ecdsaPublicKey);
    }
}
