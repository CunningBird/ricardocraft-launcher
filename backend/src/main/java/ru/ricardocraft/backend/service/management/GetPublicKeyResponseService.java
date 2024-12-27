package ru.ricardocraft.backend.service.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.dto.events.request.management.GetPublicKeyRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class GetPublicKeyResponseService extends AbstractResponseService {

    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public GetPublicKeyResponseService(ServerWebSocketHandler handler, KeyAgreementManager keyAgreementManager) {
        super(GetPublicKeyResponse.class, handler);
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public GetPublicKeyRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        return new GetPublicKeyRequestEvent(keyAgreementManager.rsaPublicKey, keyAgreementManager.ecdsaPublicKey);
    }
}
