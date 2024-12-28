package ru.ricardocraft.backend.service.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.GetPublicKeyRequest;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class GetPublicKeyService extends AbstractService {

    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public GetPublicKeyService(ServerWebSocketHandler handler, KeyAgreementManager keyAgreementManager) {
        super(GetPublicKeyRequest.class, handler);
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public GetPublicKeyResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        return new GetPublicKeyResponse(keyAgreementManager.rsaPublicKey, keyAgreementManager.ecdsaPublicKey);
    }
}
