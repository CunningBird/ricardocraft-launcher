package ru.ricardocraft.backend.controller.secure;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.service.controller.secure.VerifySecureLevelKeyService;

@Component
public class VerifySecureLevelKeyController extends AbstractController {

    private final VerifySecureLevelKeyService verifySecureLevelKeyService;

    public VerifySecureLevelKeyController(ServerWebSocketHandler handler, VerifySecureLevelKeyService verifySecureLevelKeyService) {
        super(VerifySecureLevelKeyRequest.class, handler);
        this.verifySecureLevelKeyService = verifySecureLevelKeyService;
    }

    @Override
    public VerifySecureLevelKeyResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return verifySecureLevelKeyService.verifySecureLevelKey(castRequest(request), client);
    }
}
